package com.github.mlangc.wetterfrosch.smile

import com.github.mlangc.wetterfrosch.{CrossValidator, SingleValueRegressionEvaluation}
import smile.validation
import smile.validation.{MSE, MeanAbsoluteDeviation}

object SmileCrossValidator extends CrossValidator[AbstractSmileRegressionTrainer] {
  def crossValidate(trainer: AbstractSmileRegressionTrainer,
                    trainingData: Seq[Seq[Map[String, Double]]],
                    targetCol: String, numFolds: Int, numRuns: Int): SingleValueRegressionEvaluation = {

    val (features, labels) = trainer.featuresExtractor.toFeaturesWithLabels(trainingData, targetCol)

    val results: Seq[SingleValueRegressionEvaluation] = 1.to(numRuns).par.map { _ =>
      val Array(mse, mae) = validation.cv(features, labels, numFolds,
        new MSE, new MeanAbsoluteDeviation)(trainer.trainModel)

      SingleValueRegressionEvaluation(mae = mae, mse = mse)
    }.seq

    SingleValueRegressionEvaluation.mean(results)
  }
}
