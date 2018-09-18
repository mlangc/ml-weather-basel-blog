package com.github.mlangc.wetterfrosch.smile

import smile.regression.LASSO

class SmileLassoRegressionTrainer(lambda: Double = 1.0)
  extends AbstractSmileRegressionTrainer with SimpleSmileTrainerBasedImpl {
  protected def newTrainer = new LASSO.Trainer(lambda)
}
