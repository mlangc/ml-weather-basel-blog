package com.github.mlangc.wetterfrosch.smile

import com.github.mlangc.wetterfrosch.ExportDataUtils
import com.typesafe.scalalogging.StrictLogging
import smile.regression

object ExamineTrees extends SmileLabModule with StrictLogging {
  override def timeSeriesLen: Int = 1

  def main(args: Array[String]): Unit = {
    val colNames = exportData.csvDaily.head.keySet
    val cart = regression.cart(trainFeatures, trainLabels, 23)
    val importance = ExportDataUtils.relabel(cart.importance(), colNames)
      .toSeq
      .sortBy(-_._2)

    println("Importance:")
    importance.foreach { case (key, value) =>
      println(f"  $value%.2f - $key")
    }

    val dot = cart.dot()
    val vs = extractVs(dot)

    println("Dumping in dot format:")
    println()
    println()
    println(dot)
    println()
    println("Legend:")
    println()

    println("Vx,Metric")
    colNames.toSeq
      .sorted
      .zipWithIndex
      .foreach { case (colName, i) =>
        if (vs.contains(i))
          println(s"V$i, $colName")
      }
  }

  private def extractVs(dot: String): Set[Int] = {
    val vs = """\bV\d+\b""".r

    vs.findAllIn(dot)
      .map(_.substring(1).toInt)
      .toSet
  }
}
