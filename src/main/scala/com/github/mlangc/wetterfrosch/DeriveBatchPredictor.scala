package com.github.mlangc.wetterfrosch

trait DeriveBatchPredictor { this: SingleValuePredictor =>
  def predict(seqs: Seq[Seq[Map[String, Double]]]): Seq[Double] = {
    seqs.map(predictOne)
  }
}
