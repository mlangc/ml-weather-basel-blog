package com.github.mlangc.wetterfrosch.smile

import com.github.mlangc.wetterfrosch.SingleValuePredictor
import smile.regression.Regression

class SmileRegressionSingleValuePredictor(
    smileModel: Regression[Array[Double]],
    val targetCol: String,
    toFeatures: Seq[Map[String, Double]] => Array[Double])
  extends SingleValuePredictor {

  override def predictOne(seq: Seq[Map[String, Double]]): Double = {
    smileModel.predict(toFeatures(seq))
  }

  def predict(seqs: Seq[Seq[Map[String, Double]]]): Seq[Double] = {
    smileModel.predict(seqs.map(toFeatures).toArray)
  }
}
