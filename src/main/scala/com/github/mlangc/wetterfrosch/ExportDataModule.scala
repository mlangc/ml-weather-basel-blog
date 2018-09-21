package com.github.mlangc.wetterfrosch

import com.softwaremill.macwire.wire

trait ExportDataModule {
  def seed = 42
  def timeSeriesLen = 2
  def batchSize = 64
  def targetCol = HistoryExportCols.TotalPrecipitationDailySum

  lazy val evaluator = wire[SingleValueRegressionEvaluator]
  lazy val exportData = wire[HistoryExportData]
  lazy val historyExportData = wire[HistoryExportData]
  lazy val historyExportParser = wire[HistoryExportParser]
}
