package com.github.mlangc.wetterfrosch.custom

import at.lnet.wetterfrosch.HistoryExportCols
import at.lnet.wetterfrosch.SingleValuePredictor
import at.lnet.wetterfrosch.SingleValuePredictorTrainer
import at.lnet.wetterfrosch.custom.StatHelpers.mean
import com.github.mlangc.wetterfrosch.HistoryExportCols
import com.github.mlangc.wetterfrosch.SingleValuePredictor
import com.github.mlangc.wetterfrosch.SingleValuePredictorTrainer

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
    new SingleValuePredictor {
      def targetCol: String = outerTargetCol

      def predict(seqs: Seq[Seq[Map[String, Double]]])(implicit dummy: DummyImplicit): Seq[Double] = {
        seqs.map { seq =>
          valMonthTable.getOrElse(seq.last(HistoryExportCols.Month), generalMean)
        }
      }
    }
  }
}
