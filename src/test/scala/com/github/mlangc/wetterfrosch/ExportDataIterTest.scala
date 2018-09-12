package com.github.mlangc.wetterfrosch

import com.github.mlangc.wetterfrosch.dl4j.ExportDataIter
import org.nd4j.linalg.dataset.DataSet
import org.scalatest.FreeSpec

class ExportDataIterTest extends FreeSpec {
  private lazy val csvData = new HistoryExportData().csv20180830
  private def batchSize = 32
  private def timeSeriesLen = 14
  private def csvCols = csvData.head.size
  private lazy val trainTestSplit = new TrainTestSplit(timeSeriesLen, csvData, 0)

  "with no data" in {
    val iter = new ExportDataIter(Seq(), true, batchSize)
    assert(iter.hasNext == false)
  }

  "with csv data" in {
    val trainIter = new ExportDataIter(trainTestSplit.trainingData, true, batchSize)
    val testIter = new ExportDataIter(trainTestSplit.testData, true, batchSize)

    assert(trainIter.batch() == batchSize)
    assert(testIter.batch() == batchSize)

    assert(trainIter.numExamples() + testIter.numExamples() == csvData.size - timeSeriesLen)
    assert(trainIter.totalOutcomes() + testIter.totalOutcomes() == csvData.size - timeSeriesLen)

    val firstTrainBatch: DataSet = trainIter.next()
    val firstTestBatch: DataSet = testIter.next()

    assert(firstTrainBatch != firstTestBatch)
    assert(firstTestBatch.numExamples() == batchSize)
    assert(firstTrainBatch.getFeatures.shape().toSeq == Seq(batchSize, csvCols, timeSeriesLen))
    assert(firstTestBatch.getLabels.shape().toSeq == Seq(batchSize, 1, timeSeriesLen))
    assert(firstTestBatch.getLabelsMaskArray.shape().toSeq == firstTestBatch.getLabels.shape().toSeq)

    val nextTrainBatch = trainIter.next()
    assert(firstTrainBatch != nextTrainBatch)
  }
}
