package com.github.mlangc.wetterfrosch.smile
import smile.regression.OLS

class SmileOlsTrainer extends AbstractSmileRegressionTrainer with SimpleSmileTrainerBasedImpl {
  protected def newTrainer = new OLS.Trainer()
}
