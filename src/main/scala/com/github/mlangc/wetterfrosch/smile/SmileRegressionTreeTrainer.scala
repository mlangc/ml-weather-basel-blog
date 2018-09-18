package com.github.mlangc.wetterfrosch.smile
import smile.regression.Regression
import smile.regression

class SmileRegressionTreeTrainer(maxNodes: Int = 200) extends AbstractSmileRegressionTrainer {
  protected def trainSmileRegressionModel(features: Array[Array[Double]],
                                          labels: Array[Double]): Regression[Array[Double]] = {
    regression.cart(features, labels, maxNodes)
  }
}
