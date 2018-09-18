package com.github.mlangc.wetterfrosch.smile

import smile.regression.RidgeRegression

class SmileRidgeRegressionTrainer(lambda: Double = 1)
  extends AbstractSmileRegressionTrainer with SimpleSmileTrainerBasedImpl {

  protected def newTrainer = new RidgeRegression.Trainer(lambda)
}
