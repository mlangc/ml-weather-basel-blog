package com.github.mlangc.wetterfrosch

trait DeriveSingleElemPredictor  { this: SingleValuePredictor =>
  def predictOne(seq: Seq[Map[String, Double]]): Double = {
    predict(Seq(seq)).head
  }
}
