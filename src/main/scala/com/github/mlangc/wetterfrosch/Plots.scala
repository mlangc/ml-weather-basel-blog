package com.github.mlangc.wetterfrosch

import HistoryExportCols._
import com.cibo.evilplot.colors.Color
import com.cibo.evilplot.colors.HTMLNamedColors._
import com.cibo.evilplot.displayPlot
import com.cibo.evilplot.numeric.Point
import com.cibo.evilplot.plot._
import com.cibo.evilplot.plot.aesthetics.DefaultTheme._
import com.cibo.evilplot.plot.renderers.BarRenderer
import com.cibo.evilplot.plot.renderers.PathRenderer

object Plots {
  private val csv = new HistoryExportData().csv20180830
  private val july2018Csv = csv.filter(r => r(Year) == 2018 && r(Month) == 7)

  def main(args: Array[String]): Unit = {
    displayPlot(selectedPlot)
  }

  def selectedPlot = tempJuly2018

def tempJuly2018: Plot = {
  val minTemp = july2018Csv.map(r => Point(r(Day), r(TempDailyMin)))
  val meanTemp = july2018Csv.map(r => Point(r(Day), r(TempDailyMean)))
  val maxTemp = july2018Csv.map(r => Point(r(Day), r(TempDailyMax)))

  def defRenderer(color: Color) =
    Some(PathRenderer.default(color = Some(color)))

  Overlay(
      XyPlot(minTemp, pathRenderer = defRenderer(blue)),
      XyPlot(meanTemp, pathRenderer = defRenderer(orange)),
      XyPlot(maxTemp, pathRenderer = defRenderer(red))
    ).xAxis(tickCountRange = Some(Seq(1, 31)))
    .yAxis(tickCountRange = Some(Seq(10, 35)))
    .xLabel("Day of Month")
    .yLabel("Degrees Celsius")
    .title("Min, Mean and Max Temperature July 2018")
    .frame()
}

def rainSunshineJuly2018: Plot = {
  // We want the bars for rain and sunshine both have the same maximum height.
  // This is accomplished by rescaling the precipitation by the factor below.
  // The value is negative, since we want the bars to go down.
  val scale: Double = {
    val rainMax = july2018Csv.map(_(TotalPrecipitationDaily)).max
    val sunMax = july2018Csv.map(_(SunshineDurationDaily)).max
    -sunMax/rainMax
  }

  val rain: Seq[Bar] =
    july2018Csv.map(r => Bar(r(TotalPrecipitationDaily)*scale, r(Day).toInt))
  val sunshine: Seq[Bar] =
    july2018Csv.map(r => Bar(r(SunshineDurationDaily), r(Day).toInt))

  // When generating labels for the y axis, we have to take into account,
  // that we are actually generating labels for two different metrics depending
  // on the sign of the value. Also, the scaling we introduced above to make
  // the plot symmetric has to be factored out.
  def formatLabel(v: Double): String = {
    if (v == 0) "0"
    else if (v > 0) f"${v.toInt}%dmin"
    else f"${v/scale}%1.1fmm"
  }

  Overlay(
    BarChart.custom(rain),
    BarChart.custom(sunshine,
      barRenderer = Some(BarRenderer.default(color = Some(orange))))
  ).xLabel("Day of Month")
    .yLabel("Mms of Rain/Mins of Sunshine")
    .yAxis(labelFormatter = Some(formatLabel _))
    .xAxis(labels = 1.to(31).map(_.toString))
    .title("Rain & Sunshine July 2018")
    .frame()
}

  def demonstrateEvilBugPlot: Plot = {
    val bars = Seq(0, 1, 2, 3, 2, 1, 0).zipWithIndex
      .map { case (x, i) => Bar(x, i) }

    Overlay(
      BarChart.custom(bars)
    ).yAxis(labelFormatter = Some(y => s"${y}t"))
  }

}
