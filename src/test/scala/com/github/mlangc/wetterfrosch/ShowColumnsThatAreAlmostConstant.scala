package com.github.mlangc.wetterfrosch

import com.typesafe.scalalogging.StrictLogging

object ShowColumnsThatAreAlmostConstant extends ExportDataModule with StrictLogging {
  def main(args: Array[String]): Unit = {
    val colsWithValues = findColsThatAreAlmostConstant(exportData.csvHourly)

    logger.info("These columns are almost constant:")
    colsWithValues.foreach { case (col, values) =>
        logger.info(s"  $col: $values")
    }
  }

  private def findColsThatAreAlmostConstant(rows: Seq[Map[String, Double]]): Seq[(String, Seq[Double])] = {
    val valuesPerCol: Map[String, Seq[Double]] = {
        rows.foldLeft(Map.empty[String, Set[Double]]) { (acc, row) =>
          row.keys.toSeq.map(key => key -> (acc.getOrElse(key, Set()) + row(key))).toMap
        }
      }.mapValues(_.toSeq.sorted)

    valuesPerCol.toSeq.filter(_._2.size < rows.size / 1000).sortBy(_._2.size)
  }
}
