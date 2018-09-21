package com.github.mlangc.wetterfrosch.smile

import smile.regression

class SmileRegressionTreeTrainer(maxNodes: Int = 200) extends
  AbstractSmileRegressionTrainer(regression.cart(_, _, maxNodes))
