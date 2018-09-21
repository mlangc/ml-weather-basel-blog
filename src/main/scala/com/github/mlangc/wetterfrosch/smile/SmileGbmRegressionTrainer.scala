package com.github.mlangc.wetterfrosch.smile
import smile.regression
import smile.regression.GradientTreeBoost.Loss

class SmileGbmRegressionTrainer(nTrees: Int = 500, maxNodes: Int = 6) extends
  AbstractSmileRegressionTrainer(regression.gbm(_, _, ntrees = nTrees, maxNodes = maxNodes, loss = Loss.LeastSquares))
