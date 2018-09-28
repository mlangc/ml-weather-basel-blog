package com.github.mlangc.wetterfrosch

import com.github.mlangc.wetterfrosch.dl4j.ExportDataIter
import org.nd4j.linalg.dataset.DataSet
import org.scalatest.FreeSpec

class ExportDataIterTest extends FreeSpec {
  private lazy val exportData = new HistoryExportData()
  private def csvData = exportData.csvDaily
  private def batchSize = 32
  private def timeSeriesLen = 14
  private def csvCols = exportData.csvDaily.head.size
  private lazy val labeledDataAssembler = new LabeledDataAssembler(exportData)
  private lazy val trainTestSplit = new TrainTestSplit(labeledDataAssembler.assemblyDailyData(timeSeriesLen), 0)

  "with no data" in {
    val iter = new ExportDataIter(Seq(), true, batchSize)
    assert(!iter.hasNext)
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
