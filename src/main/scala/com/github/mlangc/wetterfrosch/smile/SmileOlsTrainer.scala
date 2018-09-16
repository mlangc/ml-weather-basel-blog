package com.github.mlangc.wetterfrosch.smile
import smile.regression.OLS

class SmileOlsTrainer extends AbstractSmileRegressionSingleValueTrainer {
  protected def newTrainer = new OLS.Trainer()
}
