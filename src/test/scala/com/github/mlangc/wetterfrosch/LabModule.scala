package com.github.mlangc.wetterfrosch

import com.github.mlangc.wetterfrosch.util.UtilityModule

trait LabModule extends ExportDataModule with UtilityModule {
  def timeSeriesLen = 1
  def useHourlyData = false
  def hoursStepSize = 1

  def colNames: Set[String] = {
    val csv = {
      if (useHourlyData) exportData.csvHourly
      else exportData.csvDaily
    }

    csv.head.keySet
  }

  private def assembledData = {
    if (!useHourlyData) labeledDataAssembler.assemblyDailyData(timeSeriesLen)
    else labeledDataAssembler.assembleHourlyData(timeSeriesLen, hoursStepSize)
  }

  lazy val trainTestSplit = new TrainTestSplit(assembledData.filter(labeledDataFilter), seed)
}
