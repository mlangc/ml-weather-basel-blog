package com.github.mlangc.wetterfrosch

trait SingleValuePredictor {
  def predict(seq: Seq[Map[String, Double]]): Double = {
    predict(Seq(seq)).head
  }

  def predict(seqs: Seq[Seq[Map[String, Double]]])(implicit dummy: DummyImplicit): Seq[Double]

  def targetCol: String
}
