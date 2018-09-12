package com.github.mlangc.wetterfrosch.smile

import smile.regression.LASSO

class SmileLassoRegressionSingleValueTrainer(lambda: Double = 1.0) extends AbstractSmileRegressionSingleValueTrainer {
  protected def newTrainer = new LASSO.Trainer(lambda)
}
