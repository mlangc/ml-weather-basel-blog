package com.github.mlangc.wetterfrosch

import scala.annotation.tailrec

import _root_.smile.regression.LASSO
import at.lnet.wetterfrosch.smile.SmileUtils
import com.typesafe.scalalogging.StrictLogging
import math.abs

import com.github.mlangc.wetterfrosch.smile.SmileUtils

object FindMostRelevantFeaturesOld extends StrictLogging {
  private def targetCol = HistoryExportCols.TotalPrecipitationDaily

  def main(args: Array[String]): Unit = {
    val exportData = new HistoryExportData()
    val trainTestSplit = new TrainTestSplit(1, cleanData(exportData.csv20180830), seed = 42)
    val trainingData = trainTestSplit.trainingData
    val keys = trainingData.head.head.keySet
    logger.info(s"Working with ${trainingData.size} training examples")

    @tailrec
    def trainAndLogSelectedFeatures(lambda: Double, droppedBefore: Set[String] = Set()): Unit = {
      val trainer = new LASSO.Trainer(lambda)
      val (features, labels) = SmileUtils.toFeaturesWithLabels(trainingData, targetCol)
      val lasso = trainer.train(features, labels)
      val labeledCoeffs = ExportDataUtils.relabel(lasso.coefficients(), keys)
      val selected = labeledCoeffs.filter(c => abs(c._2) > 1e-9).map(_._1).toSet
      val dropped = labeledCoeffs.filter(c => abs(c._2) <= 1e-9).map(_._1).toSet

      if (droppedBefore != dropped) {
        logger.info(s"-----------------")
        logger.info(s"lambda = $lambda:")
        logger.info(s"  Features selected (${selected.size}): $selected")
        logger.info(s"  Features dropped (${dropped.size}): $dropped")
        selected
      }

      if (selected.size > 3) {
        trainAndLogSelectedFeatures(lambda*1.01, dropped)
      }
    }

    trainAndLogSelectedFeatures(10000)
  }

  private def cleanData(data: Seq[Map[String, Double]]) = {
    data.map { row =>
      row - HistoryExportCols.Hour - HistoryExportCols.Minute
    }
  }
}
