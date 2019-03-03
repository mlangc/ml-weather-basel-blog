package com.github.mlangc.wetterfrosch

import com.github.mlangc.wetterfrosch.util.RandomModule
import com.softwaremill.macwire.wire

import scala.util.Random

trait ExportDataModule extends RandomModule {
  def targetCol: String = HistoryExportCols.TotalPrecipitationDailySum

  lazy val historyExportRowTransformers = new HistoryExportRowTransformers()
  lazy val evaluator: SingleValueRegressionEvaluator = wire[SingleValueRegressionEvaluator]
  lazy val exportData: HistoryExportData = wire[HistoryExportData]
  lazy val historyExportParser: HistoryExportParser = wire[HistoryExportParser]
  lazy val labeledDataAssembler: LabeledDataAssembler = wire[LabeledDataAssembler]
  def labeledDataFilter: LabeledDataFilter = LabeledDataFilter.Default
}
