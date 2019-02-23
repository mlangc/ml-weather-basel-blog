package com.github.mlangc.wetterfrosch.smile
import smile.regression
import smile.regression.GradientTreeBoost.Loss

class SmileGbmRegressionTrainer(nTrees: Int = 500, maxNodes: Int = 4) extends
  AbstractSmileRegressionTrainer(
    "Gradient Boosted Trees",
    regression.gbm(_, _, ntrees = nTrees, maxNodes = maxNodes, loss = Loss.LeastSquares))
