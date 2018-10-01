package com.github.mlangc.wetterfrosch.smile

import smile.regression

class SmileRegressionTreeTrainer(maxNodes: Int = 50,
                                 override protected val featuresExtractor: SmileFeaturesExtractor = DefaultSmileFeaturesExtractor)
  extends AbstractSmileRegressionTrainer(regression.cart(_, _, maxNodes))

