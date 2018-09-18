package com.github.mlangc.wetterfrosch.smile

import smile.regression.{Regression, RegressionTrainer}

trait SimpleSmileTrainerBasedImpl { this: AbstractSmileRegressionTrainer =>
  protected def newTrainer: RegressionTrainer[Array[Double]]

  protected def trainSmileRegressionModel(features: Array[Array[Double]],
                                          labels: Array[Double]): Regression[Array[Double]] = {
    newTrainer.train(features, labels)
  }
}
