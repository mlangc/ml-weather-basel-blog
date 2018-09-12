package com.github.mlangc.wetterfrosch

trait SingleValuePredictorTrainer {
  def train(trainingData: Seq[Seq[Map[String, Double]]], targetCol: String): SingleValuePredictor
}
