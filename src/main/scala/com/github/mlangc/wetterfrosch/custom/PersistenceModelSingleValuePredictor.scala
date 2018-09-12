package com.github.mlangc.wetterfrosch.custom

import at.lnet.wetterfrosch.SingleValuePredictor
import com.github.mlangc.wetterfrosch.SingleValuePredictor

class PersistenceModelSingleValuePredictor(val targetCol: String) extends SingleValuePredictor {
  def predict(seqs: Seq[Seq[Map[String, Double]]])(implicit dummy: DummyImplicit): Seq[Double] = {
    seqs.map(_.last(targetCol))
  }
}
