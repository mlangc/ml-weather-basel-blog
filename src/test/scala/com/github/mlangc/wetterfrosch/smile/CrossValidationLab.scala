package com.github.mlangc.wetterfrosch.smile

import com.github.mlangc.wetterfrosch.{ExportDataModule, TrainTestSplit}
import com.typesafe.scalalogging.StrictLogging
import smile.regression
import smile.validation
import smile.validation.RMSE
import smile.math.Math
import com.cibo.evilplot._
import com.cibo.evilplot.colors.HTMLNamedColors.{blue, green}
import com.cibo.evilplot.colors.{Color, HTMLNamedColors}
import com.cibo.evilplot.numeric.Point
import com.cibo.evilplot.plot._
import com.cibo.evilplot.plot.aesthetics.DefaultTheme._
import com.cibo.evilplot.plot.renderers.PathRenderer
import smile.regression.Regression

object CrossValidationLab extends ExportDataModule with StrictLogging {
  private lazy val trainTestSplit = new TrainTestSplit(exportData.csvDaily, timeSeriesLen, seed)
  private lazy val (trainFeatures, trainLabels) = DefaultSmileFeaturesExtractor.toFeaturesWithLabels(trainTestSplit.trainingData, targetCol)
  private lazy val (testFeatures, testLabels) = DefaultSmileFeaturesExtractor.toFeaturesWithLabels(trainTestSplit.testData, targetCol)

  private case class Metrics(rmse: Double, mae: Double)

  def main(args: Array[String]): Unit = {
    Math.setSeed(seed)

    val cartMetrics = cvCarts(2.to(15), 30, 50)
    val best = cartMetrics.minBy(_._2.rmse)._1

    cartMetrics.foreach { case (maxNode, metrics) =>
        val prefix = if (maxNode == best) "*" else " "
        logger.info(f"$prefix$maxNode%02d, ${metrics.rmse}%.2f")
    }

    plotCartMetrics(cartMetrics)
  }

  private def cvCarts(maxNodes: Seq[Int], folds: Int, samplesPerFold: Int = 1): Seq[(Int, Metrics)] = {
    cvGeneric(maxNodes, folds, samplesPerFold)(regression.cart(_, _, _))
  }


  private def cvGeneric[ParamType](params: Seq[ParamType], folds: Int, samplesPerFold: Int)
                                  (trainWithParam: (Array[Array[Double]], Array[Double], ParamType) => Regression[Array[Double]])
  : Seq[(ParamType, Metrics)] = {
    params.par.map { param =>
      param -> mean {
        1.to(samplesPerFold).map { _ =>
          val Array(rmse, mae) = validation.cv(trainFeatures, trainLabels,
            folds,new RMSE, new MaeMeasure)(trainWithParam(_, _, param))

          Metrics(rmse, mae)
        }
      }
    }.seq
  }

  private def mean(metrics: Seq[Metrics]): Metrics = {
    val sum = metrics.foldLeft(Metrics(0, 0)) { (acc, metrics) =>
      Metrics(rmse = acc.rmse + metrics.rmse, mae = acc.mae + metrics.mae)
    }

    val n = metrics.size
    Metrics(rmse = sum.rmse/n, mae = sum.mae/n)
  }

  private def plotCartMetrics(cartMetrics: Seq[(Int, Metrics)]): Unit = {
    val rmses = cartMetrics.map { case (n, metrics) => Point(n, metrics.rmse)}
    val maes = cartMetrics.map { case (n, metrics) => Point(n, metrics.mae)}

    val plot =
      Overlay(
          XyPlot(rmses),
        ).xAxis(tickCount = Some(cartMetrics.size))
        .yAxis(tickCount = Some(5))
        .frame()
        .xLabel("Max nodes")
        .yLabel("RMSE")

    displayPlot(plot)
  }

  private def defRenderer(color: Color) =
    Some(PathRenderer.default(color = Some(color)))
}
