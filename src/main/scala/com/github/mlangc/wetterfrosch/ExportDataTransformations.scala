package com.github.mlangc.wetterfrosch

import java.time.LocalDate

import com.github.mlangc.wetterfrosch.HistoryExportCols._



object ExportDataTransformations {
  private val OffsetDays = LocalDate.of(2018, 3, 20).getDayOfYear

  def addTimeOfYearCols(row: Map[String, Double], keepOrigCols: Boolean = false): Map[String, Double] = {
    row.get(Year).flatMap { year =>
      row.get(Month).flatMap { month =>
        row.get(Day).map { day =>
          import scala.math._

          val date = LocalDate.of(year.toInt, month.toInt, day.toInt)
          val dayOfYear1 = date.getDayOfYear - 1
          val daysInYear = if (date.isLeapYear) 366 else 365
          val dayOfYear2 = (dayOfYear1 - 31) % daysInYear

          def toRad(d: Int) = 2*Pi/daysInYear * (d - OffsetDays)
          def toTimeYear(d: Int) = sin(toRad(d))

          val rowWithNewCols = row +
            (TimeOfYear1 -> toTimeYear(dayOfYear1)) +
            (TimeOfYear2 -> toTimeYear(dayOfYear2))

          if (keepOrigCols) rowWithNewCols
          else rowWithNewCols - Year - Month - Day
        }
      }
    }.getOrElse(row)
  }
}
