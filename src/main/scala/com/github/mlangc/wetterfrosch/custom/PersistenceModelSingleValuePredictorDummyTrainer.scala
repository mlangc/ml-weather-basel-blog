package com.github.mlangc.wetterfrosch.custom

import com.github.mlangc.wetterfrosch.{SingleValuePredictor, SingleValuePredictorTrainer}

class PersistenceModelSingleValuePredictorDummyTrainer extends SingleValuePredictorTrainer {
  def train(trainingData: Seq[Seq[Map[String, Double]]], targetCol: String): SingleValuePredictor = {
    new PersistenceModelSingleValuePredictor(targetCol)
  }
}
