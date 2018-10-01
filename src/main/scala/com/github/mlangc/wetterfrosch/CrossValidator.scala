package com.github.mlangc.wetterfrosch

import _root_.smile.validation.CrossValidation
import _root_.smile.math.Math

trait CrossValidator[-TrainerType <: SingleValuePredictorTrainer] {
  def crossValidate(trainer: TrainerType,
                    trainingData: Seq[Seq[Map[String, Double]]],
                    targetCol: String,
                    numFolds: Int, numRuns: Int): SingleValueRegressionEvaluation
}

object CrossValidator {
  implicit val genericCrossValidator: CrossValidator[SingleValuePredictorTrainer] =
    (trainer: SingleValuePredictorTrainer,
     trainingData: Seq[Seq[Map[String, Double]]],
     targetCol: String,
     numFolds: Int, numRuns: Int) => {

    val trainingDataArr = trainingData.toArray
    val evaluator = new SingleValueRegressionEvaluator

    val results: Seq[SingleValueRegressionEvaluation] = 1.to(numRuns).flatMap { _ =>
      val crossValidation = new CrossValidation(trainingDataArr.size, numFolds)
      0.until(numFolds).par.map { i =>
        val train = Math.slice(trainingDataArr, crossValidation.train(i))
        val test = Math.slice(trainingDataArr, crossValidation.test(i))
        val model = trainer.train(train, targetCol)
        evaluator.eval(model, test)
      }
    }

    SingleValueRegressionEvaluation.mean(results)
  }
}
