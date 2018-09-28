package com.github.mlangc.wetterfrosch

import com.softwaremill.macwire.wire

trait ExportDataModule {
  def seed = 42
  def timeSeriesLen = 2
  def batchSize = 64

  def targetCol: String = HistoryExportCols.TotalPrecipitationDailySum

  lazy val evaluator: SingleValueRegressionEvaluator = wire[SingleValueRegressionEvaluator]
  lazy val exportData: HistoryExportData = wire[HistoryExportData]
  lazy val historyExportParser: HistoryExportParser = wire[HistoryExportParser]
  lazy val labeledDataAssembler: LabeledDataAssembler = wire[LabeledDataAssembler]
}
