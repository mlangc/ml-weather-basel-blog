package com.github.mlangc.wetterfrosch.dl4j

import com.github.mlangc.wetterfrosch.ExportDataUtils
import com.github.mlangc.wetterfrosch.HistoryExportCols
import org.nd4j.linalg.api.ndarray.INDArray
import org.nd4j.linalg.dataset.DataSet
import org.nd4j.linalg.dataset.api.DataSetPreProcessor
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator
import org.nd4j.linalg.factory.Nd4j

class ExportDataIter(timeSeries: Seq[Seq[Map[String, Double]]], lastStepIsOutput: Boolean, batchSize: Int) extends DataSetIterator {
  private val indexedTimeSeries = timeSeries.toArray
  private var offset = 0
  private def n = indexedTimeSeries.length

  def next(num: Int): DataSet = {
    if (offset >= n) {
      throw new IndexOutOfBoundsException("" + offset)
    }

    val actualNum = math.min(num, n - offset)
    val slice = indexedTimeSeries.slice(offset, offset + actualNum)
    val dataSet = toDataSet(slice)

    offset += actualNum
    dataSet
  }

  def totalExamples(): Int = n

  def inputColumns(): Int = {
    indexedTimeSeries.headOption
      .flatMap(_.headOption.map(_.size))
      .getOrElse(-1)
  }

  def totalOutcomes(): Int = n

  def resetSupported(): Boolean = true

  def asyncSupported(): Boolean = false

  def reset(): Unit = offset = 0

  def batch(): Int = batchSize

  def cursor(): Int = offset

  def numExamples(): Int = n

  def setPreProcessor(preProcessor: DataSetPreProcessor): Unit = ???

  def getPreProcessor: DataSetPreProcessor = ???

  def getLabels: java.util.List[String] = null

  def hasNext: Boolean = offset < n

  def next(): DataSet = next(batchSize)

  private def toDataSet(slice: Array[Seq[Map[String, Double]]]): DataSet = {
    val dataSet = new DataSet()
    dataSet.setFeatures(toFeatures(slice))

    dataSet.setLabels(toLabels(slice, lastStepIsOutput))

    if (lastStepIsOutput) {
      dataSet.setLabelsMaskArray(toMaskArray(slice))
    }

    dataSet
  }

  private def toFeatures(slice: Array[Seq[Map[String, Double]]]): INDArray = {
    val features = Nd4j.create(slice.size, slice.head.head.size, slice.head.size - (if (lastStepIsOutput) 1 else 0))

    for {
      (timeSeries, i) <- slice.zipWithIndex
      (step, j) <- (if (lastStepIsOutput) timeSeries.init else timeSeries).zipWithIndex
      (v, k) <- ExportDataUtils.toDoubles(step).zipWithIndex
    } {
      features.putScalar(i, k, j, v)
    }

    features
  }

  private def toLabels(slice: Array[Seq[Map[String, Double]]], lastStepIsOutput: Boolean): INDArray = {
    if (!lastStepIsOutput) {
      Nd4j.zeros(slice.size, 1, slice.head.size)
    } else {
      val labels = Nd4j.zeros(slice.size, 1, slice.head.size - 1)
      val targetValues: Array[Double] = slice.map(_.last(HistoryExportCols.TotalPrecipitationDailySum))

      for ((v, i) <- targetValues.zipWithIndex) {
        labels.putScalar(i, 0, slice.head.size - 2, v)
      }

      labels
    }
  }

  private def toMaskArray(slice: Array[Seq[Map[String, Double]]]): INDArray = {
    val mask = Nd4j.zeros(slice.size, 1, slice.head.size - 1)

    for (i <- 0.until(slice.size)) {
      mask.putScalar(i, 0, slice.head.size - 2, 1.0)
    }

    mask
  }
}

