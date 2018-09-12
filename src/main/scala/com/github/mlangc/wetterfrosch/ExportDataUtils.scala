package com.github.mlangc.wetterfrosch

object ExportDataUtils {
  def toDoubles(row: Map[String, Double]): Seq[Double] = {
    row.toSeq.sortBy(_._1).map(_._2)
  }

  def relabel(values: Seq[Double], keys: Set[String]): Map[String, Double] = {
    require(keys.size == values.size)
    keys.toSeq.sorted.zip(values).toMap
  }

}
