package com.github.mlangc.wetterfrosch.smile
import smile.regression.Regression
import smile.regression
import smile.regression.GradientTreeBoost.Loss

class SmileGbmRegressionTrainer(nTrees: Int = 500, maxNodes: Int = 6) extends AbstractSmileRegressionTrainer {
  protected def trainSmileRegressionModel(features: Array[Array[Double]],
                                          labels: Array[Double]): Regression[Array[Double]] = {
    regression.gbm(features, labels, ntrees = nTrees, maxNodes = maxNodes, loss = Loss.LeastSquares)
  }
}
