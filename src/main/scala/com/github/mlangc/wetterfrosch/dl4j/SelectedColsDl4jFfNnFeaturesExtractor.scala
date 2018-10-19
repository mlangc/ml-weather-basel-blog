package com.github.mlangc.wetterfrosch.dl4j
import com.github.mlangc.wetterfrosch.ExportDataUtils._
import org.nd4j.linalg.api.ndarray.INDArray
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator

class SelectedColsDl4jFfNnFeaturesExtractor(cols: Array[Set[String]]) extends Dl4jFeaturesExtractor {
  def this(cols: Set[String]) = this(Array(cols))

  def toFeaturesWithLabels(seqs: Seq[Seq[Map[String, Double]]], targetCol: String, batchSize: Int): DataSetIterator = {
    DefaultDl4jFfNnFeaturesExtractor.toFeaturesWithLabels(selectCols(seqs, cols), targetCol, batchSize)
  }

  def toFeatures(seqs: Seq[Seq[Map[String, Double]]]): INDArray = {
    DefaultDl4jFfNnFeaturesExtractor.toFeatures(selectCols(seqs, cols))
  }
}
