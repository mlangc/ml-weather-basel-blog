package com.github.mlangc.wetterfrosch.custom

import com.github.mlangc.wetterfrosch.SingleValuePredictor
import com.github.mlangc.wetterfrosch.SingleValuePredictorTrainer
import com.github.mlangc.wetterfrosch.custom.StatHelpers.median

class MedianSingleValuePredictorTrainer extends SingleValuePredictorTrainer {
  def train(trainingData: Seq[Seq[Map[String, Double]]], targetCol: String): SingleValuePredictor = {
    val values = trainingData.map(_.last(targetCol))
    new ConstantSingleValuePredictor(targetCol, median(values.toArray))
  }

}
