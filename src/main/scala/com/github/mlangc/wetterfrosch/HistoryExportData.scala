package com.github.mlangc.wetterfrosch

class HistoryExportData(parser: HistoryExportParser = new HistoryExportParser) {
  lazy val csvDaily: Seq[Map[String, Double]] = {
    cleanDailyData(parser.parse(Resources.historyExportDaily))
  }

  lazy val csvHourly: Seq[Map[String, Double]] = {
    cleanHourlyData(parser.parse(Resources.historyExportHourly))
  }

  private def cleanDailyData(data: Seq[Map[String, Double]]) = {
    data.map { row =>
      row - HistoryExportCols.Hour - HistoryExportCols.Minute
    }
  }

  private def cleanHourlyData(data: Seq[Map[String, Double]]) = {
    data.map { row =>
      row - HistoryExportCols.Minute
    }
  }
}
