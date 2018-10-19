package com.github.mlangc.wetterfrosch.neuro.flow

import breeze.linalg.DenseVector

trait NeuroFlowFeaturesExtractor {
  def toFeaturesWithLabels(seqs: Seq[Seq[Map[String, Double]]], targetCol: String)
  : (Seq[DenseVector[Double]], Seq[DenseVector[Double]])

  def toFeatures(seqs: Seq[Seq[Map[String, Double]]]): Seq[DenseVector[Double]]
}

