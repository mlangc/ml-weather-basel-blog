package com.github.mlangc.wetterfrosch.smile

import boopickle.Default._
import com.cibo.evilplot._
import com.cibo.evilplot.numeric.Point
import com.cibo.evilplot.plot.{XyPlot, _}
import com.cibo.evilplot.plot.aesthetics.DefaultTheme._
import com.github.mlangc.wetterfrosch.util.store.DefaultPicklers._
import com.github.mlangc.wetterfrosch.util.store.StoreKey
import com.github.mlangc.wetterfrosch.{ExportDataUtils, SingleValueRegressionEvaluation}
import smile.regression

object ExperimentWithForestFeatureSelection extends SmileLabModule {
  override def timeSeriesLen: Int = 2
  def numFolds = 25
  def numRuns = 50

  def main(args: Array[String]): Unit = {
    val importanceScores = calcFeatureImportanceScores()

    importanceScores.zipWithIndex.foreach { case ((name, step, score), i) =>
        println(f"$i> $score%.1e - <$step> - $name")
    }

    val nWithRmsesWithMostImportant = 1.to(importanceScores.size).par.map { n =>
      val ev = cvOlsWithMostImportantFeatures(importanceScores, n)
      Point(n, ev.rmse)
    }.seq

    lazy val nWithRmsesWithLeastImportant = 1.to(importanceScores.size).par.map { n =>
      val ev = cvOlsWithLeastImportantFeatures(importanceScores, n)
      Point(n, ev.rmse)
    }.seq

    val min = nWithRmsesWithMostImportant.minBy(_._2)
    println(s"Min @ $min out of ${nWithRmsesWithMostImportant.size}")

    displayPlot {
      Overlay(
        XyPlot(nWithRmsesWithMostImportant),
        //XyPlot(nWithRmsesWithLeastImportant)
      ).xAxis(tickCount = Some(30))
         .xGrid(lineCount = Some(30))
         .yAxis()
         .frame()
    }
  }

  private def cvOlsWithMostImportantFeatures(importance: Seq[(String, Int, Double)], n: Int): SingleValueRegressionEvaluation = {
    val key = StoreKey(getClass, s"cvOlsWithMostImportantFeatures-$n-$timeSeriesLen-$numRuns-$suffix")
    objectStore.load(key) {
      cvOlsWithFirstFeatures(importance, n)
    }
  }

  private def cvOlsWithLeastImportantFeatures(importance: Seq[(String, Int, Double)], n: Int): SingleValueRegressionEvaluation = {
    val key = StoreKey(getClass, s"cvOlsWithLeastImportantFeatures-$n-$timeSeriesLen-$numRuns-$suffix")
    objectStore.load(key) {
      cvOlsWithFirstFeatures(importance.reverse, n)
    }
  }

  private def cvOlsWithFirstFeatures(importance: Seq[(String, Int, Double)], n: Int): SingleValueRegressionEvaluation = {
    val colsToSelect: Array[Set[String]] =
      addPlaceholdersForMissingKeys(Set.empty[String], -timeSeriesLen, -1) {
        importance.take(n)
          .groupBy(_._2)
          .mapValues(_.map(_._1).toSet)
      }.toSeq
        .sortBy(_._1)
        .map(_._2)
        .toArray

    val featuresExtractor = new SelectedColsSmileFeaturesExtractor(colsToSelect)
    val ols = new SmileOlsTrainer(featuresExtractor)
    SmileCrossValidator.crossValidate(ols, trainTestSplit.trainingData, targetCol, numFolds, numRuns)
  }

  private def addPlaceholdersForMissingKeys[T](placeholder: T, minKey: Int, maxKey: Int)(map: Map[Int, T]): Map[Int, T] = {
    minKey.to(maxKey).foldLeft(map) { (map, key) =>
      if (map.contains(key)) map
      else map.updated(key, placeholder)
    }
  }

  private def calcFeatureImportanceScores(): Seq[(String, Int, Double)] = {
    objectStore.load(featureImportanceScoreKey) {
      val forest = regression.randomForest(trainFeatures, trainLabels)
      val importance: Seq[Map[String, Double]] = ExportDataUtils.relabelFlattenedSeq(forest.importance(), colNames)

      importance.zip((-timeSeriesLen).to(-1)).flatMap { case (scores, i) =>
        scores.map { case (name, score) =>
          (name, i, score)
        }
      }.sortBy(-_._3)
    }
  }

  private def featureImportanceScoreKey = {
    StoreKey(getClass, s"feature-importance-scores-$timeSeriesLen-$suffix")
  }

  private def suffix = if (useHourlyData) "hourly" else "daily"
}
