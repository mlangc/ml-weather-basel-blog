package com.github.mlangc.wetterfrosch.smile

import com.cibo.evilplot._
import com.cibo.evilplot.colors.Color
import com.cibo.evilplot.colors.HTMLNamedColors._
import com.cibo.evilplot.numeric.Point
import com.cibo.evilplot.plot._
import com.cibo.evilplot.plot.aesthetics.DefaultTheme._
import com.cibo.evilplot.plot.renderers.PathRenderer
import com.typesafe.scalalogging.StrictLogging
import smile.regression
import smile.validation

object PlotOlsTrainTestErrorForDifferentTrainingSetSizes
  extends SmileLabModule with StrictLogging {

  override def timeSeriesLen: Int = 3
  override def seed: Int = 0xCAFEBABE

  def main(args: Array[String]): Unit = {
    val trainSetSizes = Array(
      250, 500, 1000, 2000, 3000, 4000, 5000, 6000, 7000, 8000, 9000, trainLabels.size)

    val (trainRmses: Seq[Point], testRmses: Seq[Point]) = {
      trainSetSizes.par.flatMap { trainSetSize =>
        try {
          val currentTrainFeatures = trainFeatures.take(trainSetSize)
          val currentTrainLabls = trainLabels.take(trainSetSize)
          trainAndEvaluateOls(currentTrainFeatures, currentTrainLabls)
        } catch {
          case e: IllegalArgumentException =>
            logger.warn(s"Skipping train set size $trainSetSize", e)
            None
        }
      }.seq.unzip
    }

    def namedRenderer(name: String, color: Color) = {
      Some(PathRenderer.named(name, color = color))
    }

    val plot = Overlay(
      XyPlot(dropExtremes(trainRmses), pathRenderer = namedRenderer("train", blue)),
      XyPlot(dropExtremes(testRmses), pathRenderer = namedRenderer("test", red))
    ).xLabel("Training set size")
      .yLabel("RMSE")
      .xAxis(tickCount = Some(trainSetSizes.size))
      .yAxis(tickCount = Some(10))
      .title(s"Train/Test Error OLS-$timeSeriesLen (seed = $seed)")
      .overlayLegend()

    displayPlot(plot)
  }

  private def dropExtremes(points: Seq[Point]): Seq[Point] = {
    points.filter { point =>
      if (point._2 > 6) {
        logger.warn(s"Filtering out $point")
        false
      } else {
        true
      }
    }
  }

  private def trainAndEvaluateOls(currentTrainFeatures: Array[Array[Double]],
    currentTrainLabls: Array[Double],
    useQr: Boolean = true): Option[(Point, Point)] = {
    val trainSetSize = currentTrainFeatures.size

    try {
      val method = if (useQr) "qr" else "svd"
      val model = regression.ols(currentTrainFeatures, currentTrainLabls, method)

      val trainPredictions = model.predict(currentTrainFeatures)
      val rmseTrain = validation.rmse(currentTrainLabls, trainPredictions)

      val testPredictions = model.predict(testFeatures)
      val rmseTest = validation.rmse(testLabels, testPredictions)

      Some((Point(trainSetSize, rmseTrain), Point(trainSetSize, rmseTest)))
    } catch {
      case e: IllegalArgumentException =>
        if (!useQr) {
          logger.warn(s"Skipping train set size $trainSetSize", e)
          None
        } else {
          logger.warn(s"Error fitting model with method qr; retrying with svd")
          trainAndEvaluateOls(currentTrainFeatures, currentTrainLabls, useQr = false)
        }
    }
  }
}
