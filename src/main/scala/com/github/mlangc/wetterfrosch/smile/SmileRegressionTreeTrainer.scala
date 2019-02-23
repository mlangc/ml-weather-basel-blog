package com.github.mlangc.wetterfrosch.smile

import smile.regression

class SmileRegressionTreeTrainer(maxNodes: Int = 50,
                                 override val featuresExtractor: SmileFeaturesExtractor = DefaultSmileFeaturesExtractor)
  extends AbstractSmileRegressionTrainer("Tree", regression.cart(_, _, maxNodes))

