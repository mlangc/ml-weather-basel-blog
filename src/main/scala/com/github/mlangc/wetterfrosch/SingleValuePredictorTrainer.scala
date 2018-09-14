package com.github.mlangc.wetterfrosch

trait SingleValuePredictorTrainer {
  /**
    * Trains a [[SingleValuePredictor]] from the given training data.
    *
    * @param trainingData a sequence of time series. Values from the initial
    * part of the series are used to predict the target column in the last
    * part of the series. Individual series should therefore always have at
    * least two elements.
    *
    * @param targetCol the column you want to predict
    */
  def train(trainingData: Seq[Seq[Map[String, Double]]], targetCol: String)
  : SingleValuePredictor
}