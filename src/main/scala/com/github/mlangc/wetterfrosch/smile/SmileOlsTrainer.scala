package com.github.mlangc.wetterfrosch.smile

import smile.regression.ols

class SmileOlsTrainer(override val featuresExtractor: SmileFeaturesExtractor = DefaultSmileFeaturesExtractor)
  extends AbstractSmileRegressionTrainer("OLS", ols(_, _))
