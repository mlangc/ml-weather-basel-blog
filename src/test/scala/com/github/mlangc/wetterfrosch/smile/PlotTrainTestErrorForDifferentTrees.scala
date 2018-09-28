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
import smile.regression.Regression
import smile.validation

object PlotTrainTestErrorForDifferentTrees extends SmileLabModule with StrictLogging {
  override def timeSeriesLen: Int = 1

  private case class Metrics(rmse: Double, mae: Double)

  def main(args: Array[String]): Unit = {
    val metrics: Seq[(Int, (Metrics, Metrics))] = 2.to(100, 2).par.map { maxNodes =>
      val model = regression.cart(trainFeatures, trainLabels, maxNodes)
      val trainMetrics = eval(model, trainFeatures, trainLabels)
      val testMetrics = eval(model, testFeatures, testLabels)
      maxNodes -> (trainMetrics, testMetrics)
    }.seq

    val (trainRmses: Seq[Point], testRmses: Seq[Point]) = metrics.map { case (maxNodes, (trainMetrics, testMetrics)) =>
      Point(maxNodes, trainMetrics.rmse) -> Point(maxNodes, testMetrics.rmse)
    }.unzip


    def namedRenderer(name: String, color: Color) = {
      Some(PathRenderer.named(name, color = color))
    }

    val plot = Overlay(
      XyPlot(trainRmses, pathRenderer = namedRenderer("RMSE Train", blue)),
      XyPlot(testRmses, pathRenderer = namedRenderer("RMSE Test", red))
    ).overlayLegend()
      .xAxis()
      .yAxis()
      .frame()

    displayPlot(plot)
  }

  private def eval(model: Regression[Array[Double]], features: Array[Array[Double]], labels: Array[Double]): Metrics = {
    val precitions = model.predict(features)

    Metrics(
      rmse = validation.rmse(labels, precitions),
      mae = validation.mad(labels, precitions)
    )
  }
}
