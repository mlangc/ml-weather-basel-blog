package com.github.mlangc.wetterfrosch

import java.time.LocalDate

import com.cibo.evilplot._
import com.cibo.evilplot.numeric.Point
import com.cibo.evilplot.plot._
import com.cibo.evilplot.plot.aesthetics.DefaultTheme._


object ExploreData {
  private val exportData = new HistoryExportData()
  private def csv20180830 = exportData.csvDaily

  def main(args: Array[String]): Unit = {
    displayPlot(dailyMeanTemp)
  }

  private def dailyMeanHist = {
    Histogram(csv20180830.map(_.apply(HistoryExportCols.TempDailyMean)), bins = 50)
      .xAxis(tickCount = Some(10))
      .yAxis()
      .frame()
      .xLabel(HistoryExportCols.TempDailyMean)
  }

  private def dailyPrecipitationHist = {
    Histogram(csv20180830.map(_.apply(HistoryExportCols.TotalPrecipitationDailySum)), bins = 50)
      .xAxis(tickCount = Some(10))
      .yAxis()
      .frame()
      .xLabel(HistoryExportCols.TotalPrecipitationDailySum)
  }

  private def yearlyMeanTemp = {
    val points: Seq[Point] = csv20180830
      .groupBy(_(HistoryExportCols.Year))
      .filter { case (_, vs) => vs.size >= 365 }
      .mapValues(_.map(_(HistoryExportCols.TempDailyMean)))
      .mapValues(vs => vs.sum/vs.size)
      .map { case (year, temp) => Point(year, temp)}
      .toSeq
      .sortBy(_.x)

    LinePlot(points)
      .xAxis(tickCount = Some(30))
      .yAxis()
      .frame()
      .xLabel(HistoryExportCols.Year)
      .yLabel("Yearly mean temp")
  }

  private def dailyMeanTemp = {
    val points = csv20180830
      .groupBy(r => (r(HistoryExportCols.Month), r(HistoryExportCols.Day)))
      .mapValues(vs => mean(vs.map(_(HistoryExportCols.TempDailyMean))))
      .map { case ((month, day), temp) => Point(dayOfYear(month, day), temp)}
      .toSeq
      .sortBy(_.x)

    XyPlot(points)
      .xAxis(tickCount = Some(30))
      .yAxis(tickCount = Some(5))
      .ybounds(lower = 0, upper = 25)
      .frame()
      .xLabel(HistoryExportCols.Year)
      .yLabel("Daily mean temp")
  }

  private def mean(vs: Seq[Double]): Double = {
    vs.sum/vs.size
  }

  private def dayOfYear(month: Double, day: Double): Int = {
    LocalDate.of(2000, month.toInt, day.toInt).getDayOfYear
  }
}
