package com.github.mlangc.wetterfrosch

class HistoryExportData(parser: HistoryExportParser = new HistoryExportParser,
                        historyExportRowTransformers: HistoryExportRowTransformers = new HistoryExportRowTransformers()) {

  lazy val csvDaily: Seq[Map[String, Double]] = {
    parser.parse(Resources.historyExportDaily)
      .map(historyExportRowTransformers.transformDaily)
  }

  lazy val csvHourly: Seq[Map[String, Double]] = {
    parser.parse(Resources.historyExportHourly)
      .map(historyExportRowTransformers.transformHourly)
  }
}
