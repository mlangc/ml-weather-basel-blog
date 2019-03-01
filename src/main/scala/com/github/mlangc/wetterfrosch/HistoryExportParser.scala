package com.github.mlangc.wetterfrosch

import java.net.URL

import scala.io.Source

class HistoryExportParser {
  def parse(url: URL): Seq[Map[String, Double]] = {
    val source = Source.fromURL(url, "UTF-8")
    try {
      val lines = dropPreamble(source.getLines().toSeq)
      val headerNames = splitLine(lines.head)
      val expectCols = headerNames.length

      lines.tail.zipWithIndex.map { case (line, lineNr) =>
        val cells = line.split(";").map(_.trim)
        assert(cells.length == expectCols, s"Line ${lineNr+1}: Expected $expectCols but got ${cells.length}")
        headerNames.zip(cells).toMap.mapValues(_.toDouble)
      }.toArray.toSeq
    } finally {
      source.close()
    }
  }

  private def dropPreamble(lines: Seq[String]): Seq[String] = {
    lines.dropWhile { line =>
      val cells = splitLine(line)
      cells.exists(_.isEmpty)
    }
  }

  private def splitLine(line: String): Array[String] = {
    line.split(";").map(_.trim)
  }
}
