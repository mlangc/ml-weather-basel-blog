package com.github.mlangc.wetterfrosch.smile

import com.github.mlangc.wetterfrosch.SingleValuePredictor
import smile.regression.Regression

class SmileRegressionSingleValuePredictor(model: Regression[Array[Double]],
                                          val targetCol: String,
                                          toFeatures: Seq[Map[String, Double]] => Array[Double]) extends SingleValuePredictor {

  override def predictOne(seq: Seq[Map[String, Double]]): Double = {
    model.predict(toFeatures(seq))
  }

  def predict(seqs: Seq[Seq[Map[String, Double]]]): Seq[Double] = {
    model.predict(seqs.map(toFeatures).toArray)
  }
}
