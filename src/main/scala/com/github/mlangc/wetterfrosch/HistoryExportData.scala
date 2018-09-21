package com.github.mlangc.wetterfrosch

class HistoryExportData(parser: HistoryExportParser = new HistoryExportParser) {
  lazy val csvDaily: Seq[Map[String, Double]] = {
    cleanData(parser.parse(Resources.historyExportDaily))
  }

  private def cleanData(data: Seq[Map[String, Double]]) = {
    data.map { row =>
      row - HistoryExportCols.Hour - HistoryExportCols.Minute
    }
  }
}
