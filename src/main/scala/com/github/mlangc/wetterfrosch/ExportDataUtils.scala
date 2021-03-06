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

  /** Flattens a sequence of maps of doubles into a sequence of doubles ordered by their appearance and their
    * respective keys
    */
  def toDoubles(seq: Seq[Map[String, Double]]): Seq[Double] = {
    seq.flatMap(toDoubles)
  }

  /** Undoes toDoubles using the provided keys
    */
  def relabel(values: Seq[Double], keys: Set[String]): Map[String, Double] = {
    require(keys.size == values.size)
    keys.toSeq.sorted.zip(values).toMap
  }

  /** Undoes toDoubles for sequences using the provided keys
    */
  def relabelFlattenedSeq(values: Seq[Double], keys: Set[String]): Seq[Map[String, Double]] = {
    require(values.size % keys.size == 0)
    values.sliding(keys.size, keys.size).map(relabel(_, keys)).toSeq
  }

  def localDateFrom(row: Map[String, Double]): LocalDate = {
    val (year, month, day) = yearMonthDayFrom(row)
    LocalDate.of(year, month, day)
  }

  def selectCols(seqs: Seq[Seq[Map[String, Double]]], cols: Seq[Set[String]])
                (implicit dummyImplicit: DummyImplicit): Seq[Seq[Map[String, Double]]] = {
    seqs.map(seq => selectCols(seq, cols))
  }

  def selectCols(seq: Seq[Map[String, Double]], colss: Seq[Set[String]]): Seq[Map[String, Double]] = {
    val nRows = seq.size
    val nSelects = colss.size
    assert(nRows == nSelects || nRows == nSelects + 1)

    val allKeys = seq.head.keySet
    seq.zipAll(colss, Map.empty[String, Double], allKeys)
      .map { case (row, cols) => row.filterKeys(cols) }
  }

  private def yearMonthDayFrom(row: Map[String, Double]): (Int, Int, Int) = {
    (row(Year).toInt, row(Month).toInt, row(Day).toInt)
  }

}
