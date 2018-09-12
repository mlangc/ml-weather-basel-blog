package com.github.mlangc.wetterfrosch

import scala.math.pow

class SingleValueRegressionEvaluator {
  def eval(predictor: SingleValuePredictor, testData: Seq[Seq[Map[String, Double]]]): SingleValueRegressionEvaluation = {
    lazy val predictionsWithLabels: Seq[(Double, Double)] = {
      val predictions: Seq[Double] = predictor.predict(testData.map(_.init))
      val labels: Seq[Double] = testData.map(_.last(predictor.targetCol))
      predictions.zip(labels).toArray.toSeq
    }

    new SingleValueRegressionEvaluation {
      lazy val mse = (predictionsWithLabels.foldLeft(0.0) { case (acc, (p, l)) => acc + pow(p - l, 2) }) / predictionsWithLabels.size
    }
  }
}
