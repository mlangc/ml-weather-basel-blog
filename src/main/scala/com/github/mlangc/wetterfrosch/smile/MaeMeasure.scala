package com.github.mlangc.wetterfrosch.smile

import smile.validation.RegressionMeasure

import scala.math.abs

class MaeMeasure extends RegressionMeasure {
  def measure(truth: Array[Double], prediction: Array[Double]): Double = {
    val ae = 0.until(truth.length).view
      .map(i => abs(truth(i) - prediction(i)))
      .sum

    ae / truth.length
  }
}
