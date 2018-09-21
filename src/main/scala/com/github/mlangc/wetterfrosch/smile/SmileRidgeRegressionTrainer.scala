package com.github.mlangc.wetterfrosch.smile

import smile.regression.ridge

class SmileRidgeRegressionTrainer(lambda: Double = 1)
  extends AbstractSmileRegressionTrainer(ridge(_, _, lambda))
