package com.github.mlangc.wetterfrosch.smile

import com.github.mlangc.wetterfrosch.HistoryExportColSubsets.ColsFromLastDayForTree23
import com.github.mlangc.wetterfrosch.HistoryExportCols._
import org.scalatest.FreeSpec

class SelectedColsSmileFeaturesExtractorTest extends FreeSpec with SmileLabModule {
  "with daily data" - {
    val n = 100
    lazy val labeledData = labeledDataAssembler.assemblyDailyData(1).take(n)

    "with two features" in {
      val cols = Set(SnowfallAmountDaily, MeanSeaLevelPressureDailyMean)
      val featuresExtractor = new SelectedColsSmileFeaturesExtractor(cols)
      val (features, labels) = featuresExtractor.toFeaturesWithLabels(labeledData, targetCol)

      assert(features.size == n)
      assert(labels.size == n)
      assert(features.head.size == cols.size)
      assert(features.toSeq.map(_.toSeq) == labeledData.map(seq => featuresExtractor.toFeatures(seq.init).toSeq))
    }

    "with predefined subsets" - {
      "tree23" in {
        val featuresExtractor = new SelectedColsSmileFeaturesExtractor(ColsFromLastDayForTree23)
        val (features, _) = featuresExtractor.toFeaturesWithLabels(labeledData, targetCol)
        assert(features.forall(_.size == ColsFromLastDayForTree23.size))
      }
    }
  }
}
