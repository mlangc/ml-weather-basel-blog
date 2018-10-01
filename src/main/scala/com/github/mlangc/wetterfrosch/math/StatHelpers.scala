package com.github.mlangc.wetterfrosch.math

import scala.math.pow
import scala.math.sqrt


object StatHelpers {
  def mean(values: Array[Double]): Double = {
    values.sum / values.size
  }

  def mean(values: Seq[Double]): Double = {
    mean(values.toArray)
  }

  def median(values: Array[Double]): Double = {
    val valuesSorted = values.sorted
    if (valuesSorted.size % 2 != 0) {
      valuesSorted(valuesSorted.size / 2)
    } else {
      (valuesSorted(valuesSorted.size / 2 - 1) + valuesSorted(valuesSorted.size / 2)) / 2
    }
  }

  def meanAndSd(values: Array[Double]): (Double, Double) = {
    val m = median(values)
    val ds = values.foldLeft(0.0)((acc, v) => acc + pow(m - v, 2)) / (values.length - 1)
    (m, sqrt(ds))
  }
}
