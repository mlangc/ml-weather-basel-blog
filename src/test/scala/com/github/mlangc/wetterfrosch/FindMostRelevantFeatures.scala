package com.github.mlangc.wetterfrosch

import scala.annotation.tailrec
import scala.collection.immutable.SortedSet

import _root_.smile.math.Math
import _root_.smile.regression.RidgeRegression
import _root_.smile.validation.CrossValidation
import _root_.smile.validation.MSE
import _root_.smile.validation.Validation
import com.github.mlangc.wetterfrosch.smile.SmileUtils
import com.typesafe.scalalogging.StrictLogging

object FindMostRelevantFeatures extends StrictLogging {
  private def targetCol = HistoryExportCols.TotalPrecipitationDaily
  private def seed = 42

  def main(args: Array[String]): Unit = {
    val exportData = new HistoryExportData()
    val trainTestSplit = new TrainTestSplit(1, cleanData(exportData.csvDaily), seed)
    val trainingData = trainTestSplit.trainingData
    val featureNames = trainingData.head.head.keySet
    val sortedFeatureNames = featureNames.toSeq.sorted.toIndexedSeq
    logger.info(s"Working with ${trainingData.size} training examples")

    val (trainFeatures, trainLabels) = SmileUtils.toFeaturesWithLabels(trainingData, targetCol)
    val cv = new CrossValidation(trainFeatures.length, 25, false)
    val mseAll = cvMseWithoutFeatures(cv, trainFeatures, trainLabels, sortedFeatureNames, Set())
    logger.info(s"$mseAll <-- all features")
    val (mseWithDroppedFeatures, droppedFeatures) = greedyDropFeaturesUntilMseExeedsValue(cv, trainFeatures, trainLabels, sortedFeatureNames, mseAll)
    logger.info(s"$mseWithDroppedFeatures <-- $droppedFeatures")
  }

  private def greedyDropFeaturesUntilMseExeedsValue(cv: CrossValidation,
                                                   features: Array[Array[Double]],
                                                   labels: Array[Double],
                                                   sortedFeatureNames: IndexedSeq[String],
                                                   maxMse: Double): (Double, Set[String]) = {

    val allFeatureNames = sortedFeatureNames.toSet

    @tailrec
    def loop(lastMse: Option[Double] = None, alreadyDropped: Set[String] = Set()): (Double, Set[String]) = {
      val couldStillBeDropped = allFeatureNames.diff(alreadyDropped)
      assert(couldStillBeDropped.nonEmpty)

      lastMse.foreach { lastMse =>
        logger.info(s"$lastMse (dropped ${alreadyDropped.size}/${sortedFeatureNames.size}) <- $alreadyDropped")
      }

      val dropSetCandiates = couldStillBeDropped.map(alreadyDropped + _)
      val bestCandiate: (Double, Set[String]) = dropSetCandiates.par.map { featuresToDrop =>
        val mse = cvMseWithoutFeatures(cv, features, labels, sortedFeatureNames, featuresToDrop)
        (mse, featuresToDrop)
      }.minBy(_._1)

      if (bestCandiate._1 > maxMse) lastMse.map(_ -> alreadyDropped).getOrElse(bestCandiate)
      else loop(Some(bestCandiate._1), bestCandiate._2)
    }

    loop()
  }

  private def cvMseWithoutFeatures(cv:CrossValidation,
                                   features: Array[Array[Double]],
                                   labels: Array[Double],
                                   sortedFeatureNames: IndexedSeq[String],
                                   featuresToDrop: Set[String]): Double = {
    val featureIndsToDrop = featuresToDrop.map(f => sortedFeatureNames.indexOf(f))

    val trainFeaturesReduced: Array[Array[Double]] = features
      .map { features =>
        features.view
          .zipWithIndex
          .filterNot(p => featureIndsToDrop.contains(p._2))
          .map(_._1)
          .toArray
      }

      val trainer = new RidgeRegression.Trainer(1)
      val mses = 0.until(cv.k).par.map { cvInd =>
        val trainInds = cv.train(cvInd)
        val testInds = cv.test(cvInd)

        val cvTrainFeatures = Math.slice(trainFeaturesReduced, trainInds)
        val cvTrainLabels = Math.slice(labels, trainInds)

        val cvTestFeatures = Math.slice(trainFeaturesReduced, testInds)
        val cvTestLabels = Math.slice(labels, testInds)

        val model = trainer.train(cvTrainFeatures, cvTrainLabels)
        Validation.test(model, cvTestFeatures, cvTestLabels, new MSE())
      }.sum

      mses/cv.k
  }

  private def cleanData(data: Seq[Map[String, Double]]) = {
    data.map { row =>
      row - HistoryExportCols.Hour - HistoryExportCols.Minute
    }
  }
}
