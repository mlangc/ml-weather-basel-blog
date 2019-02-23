package com.github.mlangc.wetterfrosch.smile

import smile.regression

class SmileRandomForestRegressionTrainer(nTrees: Int = 500) extends
  AbstractSmileRegressionTrainer("Random Forest", regression.randomForest(_, _, ntrees = nTrees))
