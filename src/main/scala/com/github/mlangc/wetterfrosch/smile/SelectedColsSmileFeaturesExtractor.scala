package com.github.mlangc.wetterfrosch.smile

class SelectedColsSmileFeaturesExtractor private (cols: Array[Set[String]]) extends SmileFeaturesExtractor {
  def this(cols1: Set[String], colsn: Set[String]*) = {
    this((cols1 +: colsn).toArray)
  }


  override def toFeatures(seq: Seq[Map[String, Double]]): Array[Double] = {
    assert(seq.size == cols.size)

    DefaultSmileFeaturesExtractor.toFeatures {
      seq.zip(cols).map { case (row, currentCols) =>
        row.filterKeys(currentCols)
      }
    }
  }

  override def toFeaturesWithLabels(seqs: Seq[Seq[Map[String, Double]]], targetCol: String): (Array[Array[Double]], Array[Double]) = {
    val adaptedSeqs = seqs.map { seq =>
      val last = seq.size - 1
      seq.zipWithIndex.map { case (row, i) =>
          if (i == last) row
          else row.filterKeys(cols(i))
      }
    }

    DefaultSmileFeaturesExtractor.toFeaturesWithLabels(adaptedSeqs, targetCol)
  }
}
