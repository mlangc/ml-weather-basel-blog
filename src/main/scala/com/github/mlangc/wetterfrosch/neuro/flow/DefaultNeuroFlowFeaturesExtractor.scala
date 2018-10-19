package com.github.mlangc.wetterfrosch.neuro.flow

import breeze.linalg.DenseVector
import com.github.mlangc.wetterfrosch.ExportDataUtils

object DefaultNeuroFlowFeaturesExtractor extends NeuroFlowFeaturesExtractor {
  def toFeaturesWithLabels(seqs: Seq[Seq[Map[String, Double]]], targetCol: String)
  : (Seq[DenseVector[Double]], Seq[DenseVector[Double]]) = {
    seqs.map { seq =>
      val features = DenseVector(ExportDataUtils.toDoubles(seq.init): _*)
      val label = DenseVector(seq.last(targetCol))

      (features, label)
    }.unzip
  }

  def toFeatures(seqs: Seq[Seq[Map[String, Double]]]): Seq[DenseVector[Double]] = {
    seqs.map(seq => DenseVector[Double](ExportDataUtils.toDoubles(seq): _*))
  }
}
