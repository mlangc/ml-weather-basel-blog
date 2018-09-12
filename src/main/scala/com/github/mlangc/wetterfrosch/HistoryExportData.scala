package com.github.mlangc.wetterfrosch

class HistoryExportData(parser: HistoryExportParser = new HistoryExportParser) {
  lazy val csv20180830: Seq[Map[String, Double]] = {
    parser.parse(Resources.historyExportCsv20180830)
  }
}
