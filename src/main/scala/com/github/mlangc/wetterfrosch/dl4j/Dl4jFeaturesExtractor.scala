package com.github.mlangc.wetterfrosch.dl4j

import org.nd4j.linalg.api.ndarray.INDArray
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator

trait Dl4jFeaturesExtractor {
  def toFeaturesWithLabels(seqs: Seq[Seq[Map[String, Double]]], targetCol: String, batchSize: Int = Dl4jDefaults.batchSize): DataSetIterator
  def toFeatures(seqs: Seq[Seq[Map[String, Double]]]): INDArray
}
