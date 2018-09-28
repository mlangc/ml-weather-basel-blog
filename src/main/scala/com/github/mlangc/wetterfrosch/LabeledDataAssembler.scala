package com.github.mlangc.wetterfrosch

import java.time.LocalDate

import com.github.mlangc.wetterfrosch.HistoryExportCols.Day
import com.github.mlangc.wetterfrosch.HistoryExportCols.Hour
import com.github.mlangc.wetterfrosch.HistoryExportCols.Minute
import com.github.mlangc.wetterfrosch.HistoryExportCols.Month
import com.github.mlangc.wetterfrosch.HistoryExportCols.Year
import com.github.mlangc.wetterfrosch.math.StatHelpers


class LabeledDataAssembler(exportData: HistoryExportData) {
  private lazy val hourlyDataMap: Map[LocalDate, Array[Map[String, Double]]] = {
    exportData.csvHourly
      .groupBy(ExportDataUtils.localDateFrom)
      .mapValues(_.toArray)
  }

  def assembleHourlyData(numSteps: Int, stepSize: Int): Seq[Seq[Map[String, Double]]] = {
    val hoursBack = numSteps * stepSize
    val daysBack = hoursBack / 24 + 1

    exportData.csvDaily.drop(daysBack)
      .map { rowForDay =>
        val date = ExportDataUtils.localDateFrom(rowForDay)
        val rowsForHours: Seq[Map[String, Double]] = daysBack.to(1, -1)
          .flatMap { daysBack =>
            val dateOfInterest = date.minusDays(daysBack)
            hourlyDataMap(dateOfInterest)
          }.takeRight(hoursBack)

        val featureRows = rowsForHours.sliding(stepSize, stepSize)
          .map(combineHourlyRows)
          .toSeq

        featureRows :+ rowForDay
      }
  }

  def assemblyDailyData(daysBack: Int): Seq[Seq[Map[String, Double]]] = {
    exportData.csvDaily.sliding(daysBack + 1, 1).toSeq
  }

  private def combineHourlyRows(rows: Seq[Map[String, Double]]): Map[String, Double] = {
    val keys = rows.head.keySet

    keys.map {
      case key @ (Year | Month | Day | Hour | Minute) =>
        key -> rows.last(key)

      case key =>
        key -> StatHelpers.mean(rows.map(_ (key)).toArray)
    }.toMap
  }
}
