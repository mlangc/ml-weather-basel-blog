package com.github.mlangc.wetterfrosch.smile

import com.github.mlangc.wetterfrosch.SingleValuePredictor
import com.github.mlangc.wetterfrosch.SingleValuePredictorTrainer
import com.github.mlangc.wetterfrosch.smile.SmileUtils.toFeatures
import com.github.mlangc.wetterfrosch.smile.SmileUtils.toFeaturesWithLabels
import smile.regression.RegressionTrainer

abstract class AbstractSmileRegressionSingleValueTrainer extends SingleValuePredictorTrainer {
  def train(trainingData: Seq[Seq[Map[String, Double]]], targetCol: String): SingleValuePredictor = {
    val (trainingFeatures, trainingLabels) = toFeaturesWithLabels(trainingData, targetCol)

    val trainer = newTrainer
    val regression = trainer.train(trainingFeatures, trainingLabels)
    new SmileRegressionSingleValuePredictor(regression, targetCol, toFeatures)
  }

  protected def newTrainer: RegressionTrainer[Array[Double]]
}
