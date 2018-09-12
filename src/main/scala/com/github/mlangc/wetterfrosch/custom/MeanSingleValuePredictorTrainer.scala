package com.github.mlangc.wetterfrosch.custom

import at.lnet.wetterfrosch.SingleValuePredictor
import at.lnet.wetterfrosch.SingleValuePredictorTrainer
import at.lnet.wetterfrosch.custom.StatHelpers.mean
import com.github.mlangc.wetterfrosch.SingleValuePredictor
import com.github.mlangc.wetterfrosch.SingleValuePredictorTrainer

class MeanSingleValuePredictorTrainer extends SingleValuePredictorTrainer {
  def train(trainingData: Seq[Seq[Map[String, Double]]], targetCol: String): SingleValuePredictor = {
    val values = trainingData.map(_.last(targetCol))
    return new ConstantSingleValuePredictor(targetCol, mean(values.toArray))
  }

}
