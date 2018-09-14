package com.github.mlangc.wetterfrosch.custom

import com.github.mlangc.wetterfrosch.math.StatHelpers.mean
import com.github.mlangc.wetterfrosch.{DeriveBatchPredictor, HistoryExportCols, SingleValuePredictor, SingleValuePredictorTrainer}

class MonthlyMeanSingleValuePredictorTrainer extends SingleValuePredictorTrainer {
  def train(trainingData: Seq[Seq[Map[String, Double]]], targetCol: String): SingleValuePredictor = {
    val valMonthTable: Map[Double, Double] = {
      trainingData
        .map(_.last)
        .map(r => (r(HistoryExportCols.Month), r(targetCol)))
        .groupBy(_._1)
        .mapValues(_.map(_._2).toArray)
        .mapValues(mean)
    }
    val generalMean = mean(valMonthTable.values.toArray)

    val outerTargetCol = targetCol
    new SingleValuePredictor with DeriveBatchPredictor {
      def targetCol: String = outerTargetCol

      def predictOne(seq: Seq[Map[String, Double]]): Double = {
        valMonthTable.getOrElse(seq.last(HistoryExportCols.Month), generalMean)
      }
    }
  }
}
