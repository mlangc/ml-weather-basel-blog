package com.github.mlangc.wetterfrosch.custom

object StatHelpers {
  def mean(values: Array[Double]): Double = {
    values.sum / values.size
  }

  def median(values: Array[Double]): Double = {
    val valuesSorted = values.sorted
    if (valuesSorted.size % 2 != 0) {
      valuesSorted(valuesSorted.size / 2)
    } else {
      (valuesSorted(valuesSorted.size / 2 - 1) + valuesSorted(valuesSorted.size / 2)) / 2
    }
  }
}
