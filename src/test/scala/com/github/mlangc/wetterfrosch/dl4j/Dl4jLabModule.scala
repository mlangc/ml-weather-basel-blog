package com.github.mlangc.wetterfrosch.dl4j

import com.github.mlangc.wetterfrosch.{ExportDataModule, LabModule}

trait Dl4jLabModule extends LabModule {
  lazy val featuresExtractor: Dl4jFeaturesExtractor = DefaultDl4jFfNnFeaturesExtractor
}
