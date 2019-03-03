package com.github.mlangc.wetterfrosch.util.plot

import com.cibo.evilplot.colors.HTMLNamedColors
import com.cibo.evilplot.geometry.Text
import com.cibo.evilplot.numeric.Point
import com.cibo.evilplot.plot.Plot
import com.cibo.evilplot.plot._
import com.cibo.evilplot.plot.aesthetics.DefaultTheme._
import com.cibo.evilplot.plot.renderers.PointRenderer
import com.github.mlangc.wetterfrosch.{HistoryExportCols, SingleValuePredictor}

class PlotUtils {
  def compareVisually(targetCol: String, seqs: Seq[Seq[Map[String, Double]]], predictor: SingleValuePredictor, predictors: SingleValuePredictor*): Plot = {
    def mkLabelText(msg: String) = {
      Text(msg = msg, size = 20)
    }

    val actualPoints = seqs.map { seqs =>
      val last = seqs.last
      val day = last(HistoryExportCols.Day)
      val value = last(targetCol)
      Point(day, value)
    }

    val actualValuesPlot = ScatterPlot(
      actualPoints,
      pointRenderer = Some(PointRenderer.default(color = Some(HTMLNamedColors.black), pointSize = Some(5), label = mkLabelText("Actual"))))

    val colors = Stream.continually(
        Stream(
          HTMLNamedColors.red,
          HTMLNamedColors.blue,
          HTMLNamedColors.green,
          HTMLNamedColors.orange,
          HTMLNamedColors.turquoise,
          HTMLNamedColors.tan)
      ).flatten

    val features = seqs.map(_.init)
    val days = seqs.map(_.last(HistoryExportCols.Day))
    val subPlots: Seq[Plot] = actualValuesPlot +: (predictor +: predictors).zip(colors).map { case (predictor, color) =>
      val predictions = predictor.predict(features)
      val points = days.zip(predictions).map { case (day, y) => Point(day, y) }
      ScatterPlot(
        points,
        pointRenderer = Some(PointRenderer.default(color = Some(color), label = mkLabelText(predictor.toString)))
      )
    }

    Overlay(subPlots: _*)
      .frame()
      .xAxis()
      .yAxis()
      .xbounds(days.head, days.last)
      .xLabel(HistoryExportCols.Day)
      .yLabel(targetCol)
      .overlayLegend()
  }
}

object PlotUtils {

}
