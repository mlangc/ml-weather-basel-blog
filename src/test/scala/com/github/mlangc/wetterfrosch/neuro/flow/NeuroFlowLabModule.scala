package com.github.mlangc.wetterfrosch.neuro.flow

import com.github.mlangc.wetterfrosch.LabModule

trait NeuroFlowLabModule extends LabModule {
  lazy val featuresExtractor: NeuroFlowFeaturesExtractor = DefaultNeuroFlowFeaturesExtractor
}
