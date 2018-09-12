package com.github.mlangc.wetterfrosch

import java.net.URL

import org.apache.commons.io.IOUtils

class HistoryExportParser {
  def parse(url: URL): Seq[Map[String, Double]] = {
    val lines = IOUtils.toString(url, "UTF-8").lines.toSeq
    val headerNames = lines.head.split(";").map(_.trim)
    val expectCols = headerNames.length

    lines.tail.zipWithIndex.map { case (line, lineNr) =>
      val cells = line.split(";").map(_.trim)
      assert(cells.length == expectCols, s"Line ${lineNr+1}: Expected $expectCols but got ${cells.length}")
      headerNames.zip(cells).toMap.mapValues(_.toDouble)
    }
  }
}
