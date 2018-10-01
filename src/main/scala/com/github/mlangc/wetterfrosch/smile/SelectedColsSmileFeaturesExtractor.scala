package com.github.mlangc.wetterfrosch.smile

class SelectedColsSmileFeaturesExtractor(cols: Set[String]) extends SmileFeaturesExtractor {
  override def toFeatures(seq: Seq[Map[String, Double]]): Array[Double] = {
    DefaultSmileFeaturesExtractor.toFeatures(seq.map(_.filterKeys(cols)))
  }

  override def toFeaturesWithLabels(seqs: Seq[Seq[Map[String, Double]]], targetCol: String): (Array[Array[Double]], Array[Double]) = {
    val adaptedSeqs = seqs.map { seq =>
      val last = seq.size - 1
      seq.zipWithIndex.map { case (row, i) =>
          if (i == last) row
          else row.filterKeys(cols)
      }
    }

    DefaultSmileFeaturesExtractor.toFeaturesWithLabels(adaptedSeqs, targetCol)
  }
}
