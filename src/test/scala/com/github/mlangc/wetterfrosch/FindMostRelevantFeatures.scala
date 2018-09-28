package com.github.mlangc.wetterfrosch

import _root_.smile.math.Math
import _root_.smile.regression.RidgeRegression
import _root_.smile.validation.CrossValidation
import _root_.smile.validation.RMSE
import _root_.smile.validation.Validation
import com.github.mlangc.wetterfrosch.smile.DefaultSmileFeaturesExtractor
import com.typesafe.scalalogging.StrictLogging

import scala.annotation.tailrec
import scala.util.Random
import scala.math.min

object FindMostRelevantFeatures extends StrictLogging {
  private def targetCol = HistoryExportCols.TotalPrecipitationDailySum
  private def seed = 42
  private def timeSeriesLen = 1

  private val handPickedFeatures = {
    import HistoryExportCols._
    Set(
      HighCloudCoverDailyMean,
      LowCloudCoverDailyMean,
      MeanSeaLevelPressureDailyMean,
      MediumCloudCoverDailyMean,
      RelativeHumidityDailyMean,
      SunshineDurationDailySum,
      TempDailyMean,
      TotalCloudCoverDailyMean,
      TotalPrecipitationDailySum,
      WindDirectionDailyMean10m,
      WindDirectionDailyMean80m,
      WindDirectionDailyMean900mb,
      WindGustDailyMean,
      WindSpeedDailyMean10m,
      WindSpeedDailyMean80m,
      WindSpeedDailyMean900mb
    )
  }

  def main(args: Array[String]): Unit = {
    val exportData = new HistoryExportData()
    val labeledDataAssembler = new LabeledDataAssembler(exportData)
    val trainTestSplit = new TrainTestSplit(labeledDataAssembler.assemblyDailyData(timeSeriesLen), seed)
    val trainingData = trainTestSplit.trainingData
    val featureNames = trainingData.head.head.keySet
    val sortedFeatureNames = featureNames.toSeq.sorted.toIndexedSeq

    val effectiveFeatureNames: IndexedSeq[String] = 1.to(timeSeriesLen)
      .reverse
      .flatMap(i => sortedFeatureNames.map(featureNameForTimeStep(_, -i)))

    val effectiveHandPickedFeaturesToDrop: Set[String] = {
      val featuresToDrop = featureNames.diff(handPickedFeatures).toSeq

      1.to(timeSeriesLen)
        .flatMap(i => featuresToDrop.map(featureNameForTimeStep(_, -i)))
        .toSet
    }

    logger.info(s"Working with ${trainingData.size} training examples")

    val (trainFeatures, trainLabels) = DefaultSmileFeaturesExtractor.toFeaturesWithLabels(trainingData, targetCol)
    val cv = new CrossValidation(trainFeatures.length, 25, false)
    val rmseAll = cvRmseWithoutFeatures(cv, trainFeatures, trainLabels, effectiveFeatureNames, Set())
    logger.info(s"$rmseAll <-- all features")

    val rmseHandpicked = cvRmseWithoutFeatures(cv, trainFeatures, trainLabels, effectiveFeatureNames, effectiveHandPickedFeaturesToDrop)
    logger.info(s"$rmseHandpicked <-- handpicked features")
    val (mseWithDroppedFeatures, droppedFeatures) = greedyAddFeaturesUnilNumFeatures(cv, trainFeatures, trainLabels, effectiveFeatureNames, 43)
    logger.info(s"$mseWithDroppedFeatures <-- $droppedFeatures")
  }

  private def featureNameForTimeStep(baseName: String, ts: Int): String = {
    s"<$ts> $baseName"
  }

  private def greedyAddFeaturesUnilNumFeatures(cv: CrossValidation,
                                               features: Array[Array[Double]],
                                               labels: Array[Double],
                                               sortedFeatureNames: IndexedSeq[String],
                                               numFeatures: Int): (Double, Set[String]) = {
    @tailrec
    def loop(dropped: Set[String] = sortedFeatureNames.toSet, bestCandidate: Option[(Double, Set[String])] = None): (Double, Set[String]) = {
      if (dropped.isEmpty) bestCandidate.get else {
        val nFeatures = sortedFeatureNames.size - dropped.size
        if (nFeatures >= numFeatures) bestCandidate.get else {
          val droppedCanditates = dropped.subsets(dropped.size - 1).toSeq

          val bestLocalCandidate = droppedCanditates.par
            .map(toDrop => cvRmseWithoutFeatures(cv, features, labels, sortedFeatureNames, toDrop) -> toDrop)
            .minBy(_._1)

          val newBestCandidate = bestCandidate
              .map { bestCandidate =>
                if (bestCandidate._1 <= bestLocalCandidate._1) bestCandidate
                else bestLocalCandidate
              }.getOrElse(bestLocalCandidate)

          if (!bestCandidate.contains(newBestCandidate)) {
            logger.info(s"${bestLocalCandidate._1} (dropped ${bestLocalCandidate._2.size}/${sortedFeatureNames.size}) <- ${bestLocalCandidate._2}")
          }

          loop(bestLocalCandidate._2, Some(newBestCandidate))
        }
      }
    }

    loop()
  }

  private def greedyDropFeaturesUntilRmseExeedsValue(cv: CrossValidation,
                                                     features: Array[Array[Double]],
                                                     labels: Array[Double],
                                                     sortedFeatureNames: IndexedSeq[String],
                                                     maxRmse: Double): (Double, Set[String]) = {

    val allFeatureNames = sortedFeatureNames.toSet

    @tailrec
    def loop(lastRmse: Option[Double] = None, alreadyDropped: Set[String] = Set(), bestCandidate: Option[(Double, Set[String])] = None): (Double, Set[String]) = {
      val couldStillBeDropped = allFeatureNames.diff(alreadyDropped)
      assert(couldStillBeDropped.nonEmpty)

      lastRmse.foreach { lastRmse =>
        val prefix = if (bestCandidate.map(_._1).contains(lastRmse)) "*" else ""
        logger.info(s"$prefix$lastRmse (dropped ${alreadyDropped.size}/${sortedFeatureNames.size}) <- $alreadyDropped")
      }

      val dropSetCandidates = couldStillBeDropped.map(alreadyDropped + _)
      val bestLocalCandidate: (Double, Set[String]) = dropSetCandidates.par.map { featuresToDrop =>
        val mse = cvRmseWithoutFeatures(cv, features, labels, sortedFeatureNames, featuresToDrop)
        (mse, featuresToDrop)
      }.minBy(_._1)

      if (bestLocalCandidate._1 > maxRmse) {
        bestCandidate
          .orElse(lastRmse.map(_ -> alreadyDropped))
          .getOrElse(bestLocalCandidate)
      } else {
        val newBestCandidate = bestCandidate.map { bestCandidate =>
          if (bestCandidate._1 < bestLocalCandidate._1) {
            bestCandidate
          } else {
            bestLocalCandidate
          }
        }.getOrElse(bestLocalCandidate)

        loop(Some(bestLocalCandidate._1), bestLocalCandidate._2, Some(newBestCandidate))
      }
    }

    loop()
  }

  private def randomFeatureSubsetSearch(cv: CrossValidation,
                                        features: Array[Array[Double]],
                                        labels: Array[Double],
                                        sortedFeatureNames: IndexedSeq[String],
                                        minNumFeatures: Int = 1,
                                        maxNumFeatures: Int = Integer.MAX_VALUE,
                                        iterations: Int = 1000): (Double, Set[String]) = {

    val rng = new Random(seed)

    def loop(iteration: Int = 0, bestComboSoFar: (Double, Set[String]) = (Double.MaxValue, Set())): (Double, Set[String]) = {
      if (iteration == iterations) bestComboSoFar else {
        val nFeatures = {
          val actualMax = min(sortedFeatureNames.length, maxNumFeatures)
          val diff = actualMax - minNumFeatures
          minNumFeatures + rng.nextInt(diff + 1)
        }

        val nFeaturesToDrop = sortedFeatureNames.length - nFeatures
        val featureIndicesToDrop = rng.shuffle(sortedFeatureNames.indices: Seq[Int]).take(nFeaturesToDrop)
        val featuresToDrop = featureIndicesToDrop.map(sortedFeatureNames).toSet
        val rmse = cvRmseWithoutFeatures(cv, features, labels, sortedFeatureNames, featuresToDrop)

        if (rmse < bestComboSoFar._1) {
          logger.info(s"$rmse (dropped ${featuresToDrop.size}/${sortedFeatureNames.size}) <- $featuresToDrop")
          loop(iteration + 1, (rmse, featuresToDrop))
        } else {
          loop(iteration + 1, bestComboSoFar)
        }
      }
    }

    loop()
  }

  private def cvRmseWithoutFeatures(cv:CrossValidation,
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
      val rmses = 0.until(cv.k).par.map { cvInd =>
        val trainInds = cv.train(cvInd)
        val testInds = cv.test(cvInd)

        val cvTrainFeatures = Math.slice(trainFeaturesReduced, trainInds)
        val cvTrainLabels = Math.slice(labels, trainInds)

        val cvTestFeatures = Math.slice(trainFeaturesReduced, testInds)
        val cvTestLabels = Math.slice(labels, testInds)

        val model = trainer.train(cvTrainFeatures, cvTrainLabels)
        Validation.test(model, cvTestFeatures, cvTestLabels, new RMSE())
      }.sum

      rmses/cv.k
  }

  private def cleanData(data: Seq[Map[String, Double]]) = {
    data.map { row =>
      row - HistoryExportCols.Hour - HistoryExportCols.Minute
    }
  }
}
