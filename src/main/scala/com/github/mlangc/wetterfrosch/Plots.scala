package com.github.mlangc.wetterfrosch

import HistoryExportCols._
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

  def selectedPlot = rainSunshineJuly2018

  def tempJuly2018: Plot = {
    val minTemp = july2018Csv.map(r => Point(r(Day), r(TempDailyMin)))
    val meanTemp = july2018Csv.map(r => Point(r(Day), r(TempDailyMean)))
    val maxTemp = july2018Csv.map(r => Point(r(Day), r(TempDailyMax)))

    Overlay(
        XyPlot(minTemp, pathRenderer = Some(PathRenderer.default(color = Some(blue)))),
        XyPlot(meanTemp, pathRenderer = Some(PathRenderer.default(color = Some(orange)))),
        XyPlot(maxTemp, pathRenderer = Some(PathRenderer.default(color = Some(red))))
      ).xAxis(tickCountRange = Some(Seq(1, 31)))
      .yAxis(tickCountRange = Some(Seq(10, 35)))
      .xLabel("Day of Month")
      .yLabel("Degrees Celsius")
      .title("Min, Mean and Max Temperature July 2018")
      .frame()
  }

def rainSunshineJuly2018: Plot = {
  val scale: Double = {
    val rainMax = july2018Csv.map(_(TotalPrecipitationDaily)).max
    val sunMax = july2018Csv.map(_(SunshineDurationDaily)).max
    -sunMax/rainMax
  }

  val rain = july2018Csv.map(r => Bar(r(TotalPrecipitationDaily)*scale, r(Day).toInt))
  val sunshine = july2018Csv.map(r => Bar(r(SunshineDurationDaily), r(Day).toInt))

  def formatLabel(v: Double): String = {
    if (v == 0) "0"
    else if (v > 0) f"${v.toInt}%dmin"
    else f"${v/scale}%1.1fmm"
  }

  Overlay(
    BarChart.custom(rain),
    BarChart.custom(sunshine, barRenderer = Some(BarRenderer.default(color = Some(orange))))
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
