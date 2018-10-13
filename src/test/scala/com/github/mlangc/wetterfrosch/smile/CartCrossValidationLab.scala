package com.github.mlangc.wetterfrosch.smile

import java.util.concurrent.ThreadLocalRandom

import boopickle.Default._
import com.cibo.evilplot._
import com.cibo.evilplot.colors.Color
import com.cibo.evilplot.colors.HTMLNamedColors.{black, blue, red}
import com.cibo.evilplot.geometry.{Disc, Drawable, Extent, Group}
import com.cibo.evilplot.numeric.Point
import com.cibo.evilplot.plot._
import com.cibo.evilplot.plot.aesthetics.Theme
import com.cibo.evilplot.plot.renderers.{GridLineRenderer, PathRenderer, PointRenderer}
import com.github.mlangc.wetterfrosch.evilplot.BlogTheme._
import com.github.mlangc.wetterfrosch.util.store.DefaultPicklers.boopickleAdapter
import com.github.mlangc.wetterfrosch.util.store.StoreKey
import com.typesafe.scalalogging.StrictLogging
import smile.math.Math
import smile.regression.{GradientTreeBoost, Regression}
import smile.validation.{MeanAbsoluteDeviation, RMSE}
import smile.{regression, validation}

object CartCrossValidationLab extends SmileLabModule with StrictLogging {
  override def timeSeriesLen: Int = 1
  override def seed: Int = 42
  override def useHourlyData = false

  private case class Metrics(rmse: Double, mae: Double)

  private case class RandomForestParams(ntrees: Int = 500, maxNodes: Int = -1, nodeSize: Int = 5, mtry: Int = -1, subsample: Double = 1.0) {
    override def toString: String = {
      if (copy(ntrees = 500) == RandomForestParams()) s"$ntrees"
      else this.productIterator.toSeq.mkString(",")
    }
  }

  private case class GbmParams(ntrees: Int = 500,
                               loss: GradientTreeBoost.Loss = GradientTreeBoost.Loss.LeastAbsoluteDeviation,
                               maxNodes: Int = 6,
                               shrinkage: Double = 0.05,
                               subsample: Double = 0.7) {
    override def toString: String = {
      if (copy(ntrees = 500) == GbmParams()) s"$ntrees"
      else this.productIterator.toSeq.mkString(",")
    }
  }

  private object GbmParams {
    def rand(): GbmParams = {
      val rng = ThreadLocalRandom.current()

      GbmParams(
        ntrees = 250,
        maxNodes = 7,
        shrinkage = rng.nextDouble(0.036, 0.037),
        subsample = rng.nextDouble(0.49, 0.5),
        loss = GradientTreeBoost.Loss.LeastSquares
      )
    }
  }

  private case class CombinedMetrics(
                                    cv: Metrics,
                                    train: Metrics,
                                    test: Metrics)

  def main(args: Array[String]): Unit = {
    Math.setSeed(seed)

    val metrics = cvForests(maxNodes = 2.to(100, 10), 25, 1)

    val best = metrics.minBy(_._2.cv.rmse)._1

    metrics.foreach { case (maxNode, metric) =>
        val prefix = if (maxNode == best) "*" else " "
        logger.info(f"$prefix$maxNode%02d, ${metric.cv.rmse}%.2f")
    }

    plotMetrics(metrics)
  }

  private def searchGoodGbgParams(tries: Int): Unit = {
    val paramss = Seq.fill(tries)(GbmParams.rand()).distinct
    val metrics = cvGbm(paramss, 10, 1)
      .sortBy(_._2.cv.rmse)

    logger.info("These are the results - sorted after CV RMSE:")
    metrics.foreach { case (params, metric) =>
        logger.info(f"  ${metric.cv.rmse}%.2f <- $params")
    }
  }

  private def cvForests(maxNodes: Seq[Int], folds: Int, samplesPerFold: Int): Seq[(Int, CombinedMetrics)] = {
    cvGeneric("forest", maxNodes, folds, samplesPerFold) { (features, labels, maxNodes) =>
      regression.randomForest(features, labels, maxNodes = maxNodes)
    }
  }

  private def cvGbm(params: Seq[GbmParams], folds: Int, samplesPerFold: Int): Seq[(GbmParams, CombinedMetrics)] = {
    cvGeneric("gbm", params, folds, samplesPerFold) { (features, labels, params) =>
      regression.gbm(features, labels, ntrees = params.ntrees, loss = params.loss, maxNodes = params.maxNodes, shrinkage = params.shrinkage, subsample = params.subsample)
    }
  }

  private def cvCarts(maxNodes: Seq[Int], folds: Int, samplesPerFold: Int = 1): Seq[(Int, CombinedMetrics)] = {
    cvGeneric("cart", maxNodes, folds, samplesPerFold)(regression.cart(_, _, _))
  }

  private def cvGeneric[ParamsType](algName: String, params: Seq[ParamsType], folds: Int, samplesPerFold: Int)
                                  (trainWithParam: (Array[Array[Double]], Array[Double], ParamsType) => Regression[Array[Double]])
  : Seq[(ParamsType, CombinedMetrics)] = {
    params.par.map { params =>
      params -> objectStore.load(toCvKey(algName, params, folds, samplesPerFold)) {
        val cvMetrics = mean {
          1.to(samplesPerFold).map { _ =>
            val Array(rmse, mae) = validation.cv(trainFeatures, trainLabels,
              folds, new RMSE, new MeanAbsoluteDeviation)(trainWithParam(_, _, params))

            Metrics(rmse, mae)
          }
        }

        val model = trainWithParam(trainFeatures, trainLabels, params)
        def calcMetrics(features: Array[Array[Double]], labels: Array[Double]): Metrics = {
          val predictions = model.predict(features)
          Metrics(
            rmse = validation.rmse(labels, predictions),
            mae = validation.mad(labels, predictions)
          )
        }

        CombinedMetrics(
          cv = cvMetrics,
          train = calcMetrics(trainFeatures, trainLabels),
          test = calcMetrics(testFeatures, testLabels)
        )
      }
    }.seq
  }

  private def toCvKey[ParamsType](algName: String, params: ParamsType, folds: Int, samplesPerFold: Int): StoreKey = {
    val name = s"cvRes-$algName-$folds-$samplesPerFold-$params"
    StoreKey(getClass, name)
  }

  private def mean(metrics: Seq[Metrics]): Metrics = {
    val sum = metrics.foldLeft(Metrics(0, 0)) { (acc, metrics) =>
      Metrics(rmse = acc.rmse + metrics.rmse, mae = acc.mae + metrics.mae)
    }

    val n = metrics.size
    Metrics(rmse = sum.rmse/n, mae = sum.mae/n)
  }

  private def plotMetrics(cartMetrics: Seq[(Int, CombinedMetrics)]): Unit = {
    val (cvRmses, trainRmses, testRmses) = cartMetrics.map { case (n, metrics) =>
      (Point(n, metrics.cv.rmse), Point(n, metrics.train.rmse), Point(n, metrics.test.rmse))
    }.unzip3

    def namedRenderer(name: String, color: Color) = {
      Some(PathRenderer.named(name, color = color))
    }


    def cvPointRenderer(implicit theme: Theme): Some[PointRenderer] = {
      val minRmseIndex = cvRmses.zipWithIndex.minBy(_._1._2)._2

      Some {
        new PointRenderer {
          override def legendContext: LegendContext = LegendContext.empty

          def render(plot: Plot, extent: Extent, index: Int): Drawable = {
            val size = {
              if (index != minRmseIndex) theme.elements.pointSize
              else theme.elements.pointSize * 3
            }

            if (index == minRmseIndex) Disc.centered(size).filled(black) behind Disc.centered(size/2).filled(red)
            else Disc.centered(size).filled(black)

          }
        }
      }
    }

    def cvGridRenderer: Some[GridLineRenderer] = {
      val minRmseLabel = cvRmses.minBy(_._2)._1.toInt.toString
      Some {
        (extent: Extent, label: String) => {
          if (label == minRmseLabel) GridLineRenderer.xGridLineRenderer().render(extent, label)
          else Group(Seq())
        }

      }
    }

    val plot =
      Overlay(
          XyPlot(cvRmses, pathRenderer = namedRenderer("CV RMSE", red), pointRenderer = cvPointRenderer),
          XyPlot(trainRmses, pathRenderer = namedRenderer("Train RMSE", blue)),
          //XyPlot(testRmses, pathRenderer = namedRenderer("Test RMSE", green))
        ).xAxis(tickCount = Some(cartMetrics.size))
        .yAxis(tickCount = Some(5))
        .frame()
        .xLabel("Max nodes")
        .yLabel("RMSE")
        .xGrid(lineCount = Some(48), lineRenderer = cvGridRenderer)
        .overlayLegend(0.9, 0.4)

    displayPlot(plot)
  }

  private def defRenderer(color: Color) =
    Some(PathRenderer.default(color = Some(color)))
}
