package com.github.mlangc.wetterfrosch.custom

import com.github.mlangc.wetterfrosch.{DeriveBatchPredictor, SingleValuePredictor}

class ConstantSingleValuePredictor(val targetCol: String, constant: Double)
  extends SingleValuePredictor with DeriveBatchPredictor {

  def predictOne(seq: Seq[Map[String, Double]]): Double = {
    constant
  }

  override def toString: String = {
    getClass.getSimpleName + s"($targetCol, $constant)"
  }
}
