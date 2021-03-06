package com.github.mlangc.wetterfrosch.smile

import smile.regression

class SmileLassoRegressionTrainer(lambda: Double = 1.0)
  extends AbstractSmileRegressionTrainer("Lasso Regression", regression.lasso(_, _, lambda))
