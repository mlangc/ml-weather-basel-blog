package com.github.mlangc.wetterfrosch.smile

import com.github.mlangc.wetterfrosch.HistoryExportCols._
import org.scalatest.FreeSpec

class SelectedColsSmileFeaturesExtractorTest extends FreeSpec with SmileLabModule {
  "with two features" in {
    val n = 100
    val labeledData = labeledDataAssembler.assemblyDailyData(1).take(n)
    val cols = Set(SnowfallAmountDaily, MeanSeaLevelPressureDailyMean)
    val featuresExtractor = new SelectedColsSmileFeaturesExtractor(cols)
    val (features, labels) = featuresExtractor.toFeaturesWithLabels(labeledData, targetCol)

    assert(features.size == n)
    assert(labels.size == n)
    assert(features.head.size == cols.size)
    assert(features.toSeq.map(_.toSeq) == labeledData.map(seq => featuresExtractor.toFeatures(seq.init).toSeq))
  }
}
