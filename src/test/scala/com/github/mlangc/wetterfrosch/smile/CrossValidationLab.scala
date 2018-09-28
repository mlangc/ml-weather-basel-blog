package com.github.mlangc.wetterfrosch.smile

import com.cibo.evilplot._
import com.cibo.evilplot.colors.Color
import com.cibo.evilplot.colors.HTMLNamedColors.blue
import com.cibo.evilplot.colors.HTMLNamedColors.green
import com.cibo.evilplot.colors.HTMLNamedColors.red
import com.cibo.evilplot.numeric.Point
import com.cibo.evilplot.plot._
import com.cibo.evilplot.plot.aesthetics.DefaultTheme._
import com.cibo.evilplot.plot.renderers.PathRenderer
import com.typesafe.scalalogging.StrictLogging
import smile.math.Math
import smile.regression
import smile.regression.Regression
import smile.validation
import smile.validation.MeanAbsoluteDeviation
import smile.validation.RMSE

object CrossValidationLab extends SmileLabModule with StrictLogging {
  override def timeSeriesLen: Int = 1
  override def seed: Int = 42

  private case class Metrics(rmse: Double, mae: Double)

  private case class CombinedMetrics(
                                    cv: Metrics,
                                    train: Metrics,
                                    test: Metrics)

  def main(args: Array[String]): Unit = {
    Math.setSeed(seed)

    val cartMetrics = cvCarts(2.to(100, 4), 10, 5)
    val best = cartMetrics.minBy(_._2.cv.rmse)._1

    cartMetrics.foreach { case (maxNode, metrics) =>
        val prefix = if (maxNode == best) "*" else " "
        logger.info(f"$prefix$maxNode%02d, ${metrics.cv.rmse}%.2f")
    }

    plotCartMetrics(cartMetrics)
  }

  private def cvCarts(maxNodes: Seq[Int], folds: Int, samplesPerFold: Int = 1): Seq[(Int, CombinedMetrics)] = {
    cvGeneric(maxNodes, folds, samplesPerFold)(regression.cart(_, _, _))
  }


  private def cvGeneric[ParamType](params: Seq[ParamType], folds: Int, samplesPerFold: Int)
                                  (trainWithParam: (Array[Array[Double]], Array[Double], ParamType) => Regression[Array[Double]])
  : Seq[(ParamType, CombinedMetrics)] = {
    params.par.map { param =>
      val cvMetrics = mean {
        1.to(samplesPerFold).map { _ =>
          val Array(rmse, mae) = validation.cv(trainFeatures, trainLabels,
            folds, new RMSE, new MeanAbsoluteDeviation)(trainWithParam(_, _, param))

          Metrics(rmse, mae)
        }
      }

      val model = trainWithParam(trainFeatures, trainLabels, param)
      def calcMetrics(features: Array[Array[Double]], labels: Array[Double]): Metrics = {
        val predictions = model.predict(features)
        Metrics(
          rmse = validation.rmse(labels, predictions),
          mae = validation.mad(labels, predictions)
        )
      }

      param -> CombinedMetrics(
        cv = cvMetrics,
        train = calcMetrics(trainFeatures, trainLabels),
        test = calcMetrics(testFeatures, testLabels)
      )
    }.seq
  }

  private def mean(metrics: Seq[Metrics]): Metrics = {
    val sum = metrics.foldLeft(Metrics(0, 0)) { (acc, metrics) =>
      Metrics(rmse = acc.rmse + metrics.rmse, mae = acc.mae + metrics.mae)
    }

    val n = metrics.size
    Metrics(rmse = sum.rmse/n, mae = sum.mae/n)
  }

  private def plotCartMetrics(cartMetrics: Seq[(Int, CombinedMetrics)]): Unit = {
    val (cvRmses, trainRmses, testRmses) = cartMetrics.map { case (n, metrics) =>
      (Point(n, metrics.cv.rmse), Point(n, metrics.train.rmse), Point(n, metrics.test.rmse))
    }.unzip3

    def namedRenderer(name: String, color: Color) = {
      Some(PathRenderer.named(name, color = color))
    }

    val plot =
      Overlay(
          XyPlot(cvRmses, pathRenderer = namedRenderer("CV RMSE", red)),
          XyPlot(trainRmses, pathRenderer = namedRenderer("Train RMSE", blue)),
          XyPlot(testRmses, pathRenderer = namedRenderer("Test RMSE", green))
        ).xAxis(tickCount = Some(cartMetrics.size))
        .yAxis(tickCount = Some(5))
        .frame()
        .xLabel("Max nodes")
        .yLabel("RMSE")
        .overlayLegend()

    displayPlot(plot)
  }

  private def defRenderer(color: Color) =
    Some(PathRenderer.default(color = Some(color)))
}
