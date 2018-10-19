package com.github.mlangc.wetterfrosch.smile

import com.github.mlangc.wetterfrosch.ExportDataUtils

class SelectedColsSmileFeaturesExtractor private (cols: Array[Set[String]]) extends SmileFeaturesExtractor {
  require(cols.nonEmpty)

  def this(cols1: Set[String], colsn: Set[String]*) = {
    this((cols1 +: colsn).toArray)
  }

  def this(cols: Seq[Set[String]]) = {
    this(cols.toArray)
  }

  override def toFeatures(seq: Seq[Map[String, Double]]): Array[Double] = {
    DefaultSmileFeaturesExtractor.toFeatures {
      ExportDataUtils.selectCols(seq, cols)
    }
  }

  override def toFeaturesWithLabels(seqs: Seq[Seq[Map[String, Double]]], targetCol: String): (Array[Array[Double]], Array[Double]) = {
    val adaptedSeqs = ExportDataUtils.selectCols(seqs, cols)
    DefaultSmileFeaturesExtractor.toFeaturesWithLabels(adaptedSeqs, targetCol)
  }
}
