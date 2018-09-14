package com.github.mlangc.wetterfrosch.custom

import com.github.mlangc.wetterfrosch.SingleValuePredictor

class ConstantSingleValuePredictor(val targetCol: String, constant: Double)
  extends SingleValuePredictor {

  def predict(seqs: Seq[Seq[Map[String, Double]]]): Seq[Double] = {
    seqs.map(_ => constant)
  }

  override def toString: String = {
    getClass.getSimpleName + s"($targetCol, $constant)"
  }
}
