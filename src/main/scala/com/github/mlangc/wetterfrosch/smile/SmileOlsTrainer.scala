package com.github.mlangc.wetterfrosch.smile

import smile.regression.ols

class SmileOlsTrainer(override protected val featuresExtractor: SmileFeaturesExtractor = DefaultSmileFeaturesExtractor)
  extends AbstractSmileRegressionTrainer(ols(_, _))
