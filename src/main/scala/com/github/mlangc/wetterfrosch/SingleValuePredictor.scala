package com.github.mlangc.wetterfrosch

trait SingleValuePredictor {
  /**
    * The name of the column we want to predict
    */
  def targetCol: String

  /**
    * Makes a single prediction from a time series
    */
  def predictOne(seq: Seq[Map[String, Double]]): Double

  /**
    * Makes predictions from multiple time series
    */
  def predict(seqs: Seq[Seq[Map[String, Double]]]): Seq[Double]

  override def toString: String = {
    getClass.getSimpleName + s"($targetCol)"
  }
}
