package com.github.mlangc.wetterfrosch

class HistoryExportData(parser: HistoryExportParser = new HistoryExportParser) {
  lazy val csvDaily: Seq[Map[String, Double]] = {
    parser.parse(Resources.historyExportDaily)
  }
}
