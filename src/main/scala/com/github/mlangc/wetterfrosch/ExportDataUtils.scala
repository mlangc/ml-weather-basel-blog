package com.github.mlangc.wetterfrosch

import java.time.LocalDate

import com.github.mlangc.wetterfrosch.HistoryExportCols.Day
import com.github.mlangc.wetterfrosch.HistoryExportCols.Month
import com.github.mlangc.wetterfrosch.HistoryExportCols.Year

object ExportDataUtils {
  /** Flattens a map of doubles into a sequence of doubles ordered by their
    * respective keys
    */
  def toDoubles(row: Map[String, Double]): Seq[Double] = {
    row.toSeq.sortBy(_._1).map(_._2)
  }

  /** Undoes toDoubles using the provided keys
    */
  def relabel(values: Seq[Double], keys: Set[String]): Map[String, Double] = {
    require(keys.size == values.size)
    keys.toSeq.sorted.zip(values).toMap
  }

  def localDateFrom(row: Map[String, Double]): LocalDate = {
    val (year, month, day) = yearMonthDayFrom(row)
    LocalDate.of(year, month, day)
  }

  private def yearMonthDayFrom(row: Map[String, Double]): (Int, Int, Int) = {
    (row(Year).toInt, row(Month).toInt, row(Day).toInt)
  }

}
