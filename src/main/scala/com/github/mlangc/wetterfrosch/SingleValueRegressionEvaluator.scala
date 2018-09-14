package com.github.mlangc.wetterfrosch

import scala.math.pow
import scala.math.abs

class SingleValueRegressionEvaluator {
  def eval(predictor: SingleValuePredictor, testData: Seq[Seq[Map[String, Double]]]): SingleValueRegressionEvaluation = {
    lazy val predictionsWithLabels: Seq[(Double, Double)] = {
      val predictions: Seq[Double] = predictor.predict(testData.map(_.init))
      val labels: Seq[Double] = testData.map(_.last(predictor.targetCol))
      predictions.zip(labels).toArray.toSeq
    }

    val n = predictionsWithLabels.size

    def calcMeanCostFromDiffs(costFun: Double => Double): Double = {
      (predictionsWithLabels.foldLeft(0.0) { case (acc, (p, l)) => acc + costFun(p - l) }) / n
    }

    new SingleValueRegressionEvaluation {
      lazy val mse = calcMeanCostFromDiffs(pow(_, 2))
      lazy val mae = calcMeanCostFromDiffs(abs)
    }
  }
}
