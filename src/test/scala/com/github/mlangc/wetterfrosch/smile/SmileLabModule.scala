package com.github.mlangc.wetterfrosch.smile

import com.github.mlangc.wetterfrosch.LabModule

trait SmileLabModule extends LabModule {
  def featuresExtractor: SmileFeaturesExtractor = DefaultSmileFeaturesExtractor
  lazy val (trainFeatures, trainLabels) = featuresExtractor.toFeaturesWithLabels(trainTestSplit.trainingData, targetCol)
  lazy val (testFeatures, testLabels) = featuresExtractor.toFeaturesWithLabels(trainTestSplit.testData, targetCol)
}
