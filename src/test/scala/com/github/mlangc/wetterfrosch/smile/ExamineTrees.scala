package com.github.mlangc.wetterfrosch.smile

import com.github.mlangc.wetterfrosch.ExportDataUtils
import com.typesafe.scalalogging.StrictLogging
import smile.regression

object ExamineTrees extends SmileLabModule with StrictLogging {
  override def timeSeriesLen: Int = 9

  def main(args: Array[String]): Unit = {
    val colNames = exportData.csvDaily.head.keySet
    val cart = regression.cart(trainFeatures, trainLabels, 100)
    val importancePerTimeStep = ExportDataUtils.relabelFlattenedSeq(cart.importance(), colNames)

    val sortedImportance = importancePerTimeStep
      .zipWithIndex
      .flatMap { case (row, i) =>
        row.toSeq
          .map { case (key, value) => (s"<${i - timeSeriesLen}> - $key", value) }
      }
      .sortBy(-_._2)

    val pasteAsScalaImportance = importancePerTimeStep
        .map(ts => ts.filter(_._2 > 0).keySet)

    println("Importance:")
    println("  Sorted:")
    sortedImportance.foreach { case (key, value) =>
      println(f"    $value%.2f - $key")
    }
    println()
    println("  To paste as Scala:")
    pasteAsScalaImportance.foreach { set =>
      println("    " + set.map("\"" + _ + "\"").mkString("Set(", ", ", "),"))
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
