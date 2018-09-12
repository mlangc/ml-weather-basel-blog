package com.github.mlangc.wetterfrosch.smile

import com.github.mlangc.wetterfrosch.ExportDataUtils

object SmileUtils {
  def toFeaturesWithLabels(seqs: Seq[Seq[Map[String, Double]]], targetCol: String): (Array[Array[Double]], Array[Double]) = {
    seqs.map { seq =>
      val features = toFeatures(seq.init)
      val label = seq.last(targetCol)

      (features, label)
    }.toArray.unzip
  }

  def toFeatures(seq: Seq[Map[String, Double]]): Array[Double] = {
    seq
      .flatMap(ExportDataUtils.toDoubles)
      .toArray
  }
}
