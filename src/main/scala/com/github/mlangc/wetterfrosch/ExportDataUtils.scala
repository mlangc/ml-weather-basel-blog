package com.github.mlangc.wetterfrosch

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
}
