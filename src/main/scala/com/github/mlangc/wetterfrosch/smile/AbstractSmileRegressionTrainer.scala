package com.github.mlangc.wetterfrosch.smile

import com.github.mlangc.wetterfrosch.SingleValuePredictor
import com.github.mlangc.wetterfrosch.SingleValuePredictorTrainer
import smile.regression.Regression

abstract class AbstractSmileRegressionTrainer(
  /** Performs the training with the given features and labels
    */
  val trainModel: (Array[Array[Double]], Array[Double]) => Regression[Array[Double]]
) extends SingleValuePredictorTrainer {

  def train(trainingData: Seq[Seq[Map[String, Double]]], targetCol: String)
  : SingleValuePredictor = {
    val (trainingFeatures, trainingLabels) =
      featuresExtractor.toFeaturesWithLabels(trainingData, targetCol)

    val regression = trainModel(trainingFeatures, trainingLabels)
    new SmileRegressionSingleValuePredictor(
      regression, targetCol, featuresExtractor.toFeatures)
  }

  def featuresExtractor: SmileFeaturesExtractor =
    DefaultSmileFeaturesExtractor
}
