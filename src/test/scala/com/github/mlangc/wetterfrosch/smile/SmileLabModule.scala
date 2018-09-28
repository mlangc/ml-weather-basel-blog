package com.github.mlangc.wetterfrosch.smile

import com.github.mlangc.wetterfrosch.ExportDataModule
import com.github.mlangc.wetterfrosch.TrainTestSplit

trait SmileLabModule extends ExportDataModule {
  def timeSeriesLen = 1
  lazy val trainTestSplit = new TrainTestSplit(labeledDataAssembler.assemblyDailyData(timeSeriesLen), seed)
  def featuresExtractor: SmileFeaturesExtractor = DefaultSmileFeaturesExtractor
  lazy val (trainFeatures, trainLabels) = featuresExtractor.toFeaturesWithLabels(trainTestSplit.trainingData, targetCol)
  lazy val (testFeatures, testLabels) = featuresExtractor.toFeaturesWithLabels(trainTestSplit.testData, targetCol)
}
