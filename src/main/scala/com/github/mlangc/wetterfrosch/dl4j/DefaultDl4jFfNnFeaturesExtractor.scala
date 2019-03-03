package com.github.mlangc.wetterfrosch.dl4j
import com.github.mlangc.wetterfrosch.ExportDataUtils
import org.deeplearning4j.datasets.iterator.impl.ListDataSetIterator
import org.nd4j.linalg.api.ndarray.INDArray
import org.nd4j.linalg.dataset.DataSet
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator
import org.nd4j.linalg.factory.Nd4j

object DefaultDl4jFfNnFeaturesExtractor extends Dl4jFeaturesExtractor {
  def toFeaturesWithLabels(seqs: Seq[Seq[Map[String, Double]]], targetCol: String, batchSize: Int): DataSetIterator with HasShuffleSupport = {
    val numExamples = seqs.size
    val numFeatures = seqs.head.init.map(_.keys.size).sum

    val featureMat = Nd4j.create(numExamples, numFeatures)
    val labelsVec = Nd4j.create(numExamples, 1)

    seqs.zipWithIndex.foreach { case (seq, i) =>
      val label = seq.last(targetCol)
      labelsVec.putScalar(i, 0, label)

      ExportDataUtils.toDoubles(seq.init).zipWithIndex.foreach { case (d, j) =>
        featureMat.putScalar(i, j, d)
      }
    }

    new ListDataSetIteratorWithShuffleSupport(new DataSet(featureMat, labelsVec).asList(), batchSize)
  }

  def toFeatures(seqs: Seq[Seq[Map[String, Double]]]): INDArray = {
    val numExamples = seqs.size
    val numFeatures = seqs.head.map(_.keys.size).sum
    val featureArr = Nd4j.create(numExamples, numFeatures)

    seqs.zipWithIndex.foreach { case (seq, i) =>
      ExportDataUtils.toDoubles(seq.init).zipWithIndex.foreach { case (d, j) =>
        featureArr.putScalar(i, j, d)
      }
    }

    featureArr
  }
}
