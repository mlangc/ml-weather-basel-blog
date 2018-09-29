package com.github.mlangc.wetterfrosch.smile

import smile.regression

class SmileRegressionTreeTrainer(maxNodes: Int = 50) extends
  AbstractSmileRegressionTrainer(regression.cart(_, _, maxNodes))

