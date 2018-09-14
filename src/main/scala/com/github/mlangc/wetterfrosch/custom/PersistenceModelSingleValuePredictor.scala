package com.github.mlangc.wetterfrosch.custom

import com.github.mlangc.wetterfrosch.{DeriveBatchPredictor, SingleValuePredictor}

class PersistenceModelSingleValuePredictor(val targetCol: String) extends
  SingleValuePredictor with DeriveBatchPredictor {

  def predictOne(seq: Seq[Map[String, Double]]): Double = {
    seq.last(targetCol)
  }
}
