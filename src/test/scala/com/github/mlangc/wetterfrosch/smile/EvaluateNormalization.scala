package com.github.mlangc.wetterfrosch.smile

import com.github.mlangc.wetterfrosch.math.StatHelpers
import com.github.mlangc.wetterfrosch.{HistoryExportCols, HistoryExportData, TrainTestSplit}
import com.typesafe.scalalogging.StrictLogging
import smile.regression
import smile.validation.{RMSE, cv}

import scala.math._

object EvaluateNormalization extends StrictLogging {
  private def timeSeriesLen = 1
  private def seed = 42
  private def targetCol = HistoryExportCols.TotalPrecipitationDailySum

  def main(args: Array[String]): Unit = {
    val exportData = new HistoryExportData()
    val trainTestSplit = new TrainTestSplit(cleanData(exportData.csvDaily), timeSeriesLen, seed)
    val (features, labels) = SmileUtils.toFeaturesWithLabels(trainTestSplit.trainingData, targetCol)
    val featuresNormalized = normalize(features)

    val folds = 20
    val nFolds = 30

    val results: Seq[(Double, Double)] = Seq(features, featuresNormalized).map { features =>
      val rmses = 1.to(folds).map { _ =>
        cv(features, labels, nFolds, new RMSE())(regression.ols(_, _))(0)
      }

      val z = 1.96
      StatHelpers.meanAndSd(rmses.toArray)
    }

    val (diff, se) = results.reduce { (l, r)  =>
      val (m1, s1) = l
      val (m2, s2) = r
      (m1 - m2, sqrt(pow(s1, 2)/nFolds + pow(s2, 2)/nFolds))
    }

    logger.info(s"$diff +- ${2*se}")
  }

  private def normalize(features: Array[Array[Double]]): Array[Array[Double]] = {
    val scale: Array[Double] = features.reduce { (left, right) =>
      left.zip(right).map { case (l, r) =>
        val al = abs(l)
        val ar = abs(r)
        if (al > ar) al else ar
      }
    }

    features.map { features =>
      features.zip(scale).map { case (f, s) =>
        if (s == 0) 0.0 else f/s
      }
    }
  }

  private def cleanData(data: Seq[Map[String, Double]]) = {
    data.map { row =>
      row - HistoryExportCols.Hour - HistoryExportCols.Minute
    }
  }

}
