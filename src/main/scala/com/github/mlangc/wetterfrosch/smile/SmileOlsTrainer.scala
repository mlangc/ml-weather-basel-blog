package com.github.mlangc.wetterfrosch.smile

import smile.regression.ols

class SmileOlsTrainer extends AbstractSmileRegressionTrainer(ols(_, _))
