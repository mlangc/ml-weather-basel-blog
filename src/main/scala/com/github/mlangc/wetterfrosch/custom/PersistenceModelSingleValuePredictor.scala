package com.github.mlangc.wetterfrosch.custom

import com.github.mlangc.wetterfrosch.SingleValuePredictor

class PersistenceModelSingleValuePredictor(val targetCol: String) extends
  SingleValuePredictor {

  def predict(seqs: Seq[Seq[Map[String, Double]]]): Seq[Double] = {
    seqs.map(_.last(targetCol))
  }
}
