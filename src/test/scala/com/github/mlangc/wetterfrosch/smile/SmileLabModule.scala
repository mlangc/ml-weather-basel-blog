package com.github.mlangc.wetterfrosch.smile

import com.github.mlangc.wetterfrosch.{ExportDataModule, TrainTestSplit}
import com.github.mlangc.wetterfrosch.util.UtilityModule

trait SmileLabModule extends ExportDataModule with UtilityModule {
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
  def featuresExtractor: SmileFeaturesExtractor = DefaultSmileFeaturesExtractor
  lazy val (trainFeatures, trainLabels) = featuresExtractor.toFeaturesWithLabels(trainTestSplit.trainingData, targetCol)
  lazy val (testFeatures, testLabels) = featuresExtractor.toFeaturesWithLabels(trainTestSplit.testData, targetCol)
}
