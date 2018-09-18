package com.github.mlangc.wetterfrosch.smile

import com.github.mlangc.wetterfrosch.SingleValuePredictor
import com.github.mlangc.wetterfrosch.SingleValuePredictorTrainer
import com.github.mlangc.wetterfrosch.smile.SmileUtils.toFeatures
import com.github.mlangc.wetterfrosch.smile.SmileUtils.toFeaturesWithLabels
import smile.regression.{Regression, RegressionTrainer}

abstract class AbstractSmileRegressionTrainer extends
  SingleValuePredictorTrainer {

  def train(trainingData: Seq[Seq[Map[String, Double]]], targetCol: String)
  : SingleValuePredictor = {
    val (trainingFeatures, trainingLabels) =
      toFeaturesWithLabels(trainingData, targetCol)

    val regression = trainSmileRegressionModel(trainingFeatures, trainingLabels)
    new SmileRegressionSingleValuePredictor(regression, targetCol, toFeatures)
  }

  protected def trainSmileRegressionModel(features: Array[Array[Double]],
                                          labels: Array[Double]): Regression[Array[Double]]
}
