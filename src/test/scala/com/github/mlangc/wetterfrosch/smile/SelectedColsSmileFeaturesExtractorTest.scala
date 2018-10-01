package com.github.mlangc.wetterfrosch.smile

import com.github.mlangc.wetterfrosch.{HistoryExportColSubsets, HistoryExportCols}
import com.github.mlangc.wetterfrosch.HistoryExportColSubsets.ColsFromLastDayForTree23
import com.github.mlangc.wetterfrosch.HistoryExportCols._
import org.scalatest.FreeSpec

class SelectedColsSmileFeaturesExtractorTest extends FreeSpec with SmileLabModule {
  "with daily data" - {
    val n = 100
    "going back one day" - {
      lazy val labeledData = labeledDataAssembler.assemblyDailyData(1).take(n)

      "with two features" in {
        val cols = Set(SnowfallAmountDaily, MeanSeaLevelPressureDailyMean)
        val featuresExtractor = new SelectedColsSmileFeaturesExtractor(cols)
        checkedToFeaturesWithLabels(featuresExtractor, labeledData)
      }

      "with predefined subsets" - {
        "tree23" in {
          val featuresExtractor = new SelectedColsSmileFeaturesExtractor(ColsFromLastDayForTree23)
          val features = checkedToFeaturesWithLabels(featuresExtractor, labeledData)
          assert(features.forall(_.size == ColsFromLastDayForTree23.size))
        }
      }
    }

    "going back 3 days" - {
      lazy val labeledData = labeledDataAssembler.assemblyDailyData(3).take(n)

      "taking only data from the last day" in {
        val featuresExtractor = new SelectedColsSmileFeaturesExtractor(Set(), Set(), ColsFromLastDayForTree23)
        val features = checkedToFeaturesWithLabels(featuresExtractor, labeledData)
        assert(features.head.size == ColsFromLastDayForTree23.size)
      }

      "taking a single column from every day" in {
        val featuresExtractor = new SelectedColsSmileFeaturesExtractor(
          Set(TotalPrecipitationDailySum), Set(MeanSeaLevelPressureDailyMin), Set(HighCloudCoverDailyMean))

        val features = checkedToFeaturesWithLabels(featuresExtractor, labeledData)
        assert(features.head.size == 3)
        assert(features.head(0) == labeledData.head(0)(TotalPrecipitationDailySum))
        assert(features.head(1) == labeledData.head(1)(MeanSeaLevelPressureDailyMin))
        assert(features.head(2) == labeledData.head(2)(HighCloudCoverDailyMean))
      }

      "taking one column from the third day and two columns from the last day" in {
        val featuresExtractor = new SelectedColsSmileFeaturesExtractor(
          Set(TotalPrecipitationDailySum), Set(), Set(HighCloudCoverDailyMean, TotalCloudCoverDailyMean))

        val features = checkedToFeaturesWithLabels(featuresExtractor, labeledData)
        assert(features.head.size == 3)
        assert(features.last(0) == labeledData.last(0)(TotalPrecipitationDailySum))
        assert(features.last(1) == labeledData.last(2)(HighCloudCoverDailyMean))
        assert(features.last(2) == labeledData.last(2)(TotalCloudCoverDailyMean))
      }
    }
  }

  private def checkedToFeaturesWithLabels(featuresExtractor: SmileFeaturesExtractor,
                                          labeledData: Seq[Seq[Map[String, Double]]]): Array[Array[Double]] = {

    val (features, labels) = featuresExtractor.toFeaturesWithLabels(labeledData, targetCol)
    assert(features.size == labeledData.size)
    assert(labels.size == features.size)
    assert(features.toSeq.map(_.toSeq) == labeledData.map(seq => featuresExtractor.toFeatures(seq.init).toSeq))
    assert(features.map(_.size).toSet.size == 1)
    features
  }
}
