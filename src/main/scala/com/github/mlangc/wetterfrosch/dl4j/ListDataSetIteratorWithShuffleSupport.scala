package com.github.mlangc.wetterfrosch.dl4j

import java.util

import org.deeplearning4j.datasets.iterator.impl.ListDataSetIterator
import org.nd4j.linalg.dataset.DataSet
import org.nd4j.linalg.dataset.api.DataSetPreProcessor
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator

import scala.collection.JavaConverters._

import scala.util.Random

class ListDataSetIteratorWithShuffleSupport(coll: util.Collection[DataSet], batchSize: Int) extends DataSetIterator with HasShuffleSupport {
  private val data = coll.toArray(new Array[DataSet](0))
  private var underlyingIter = new ListDataSetIterator[DataSet](coll, batchSize)

  private def impl = synchronized(underlyingIter)

  def next(num: Int): DataSet = impl.next(num)

  def totalExamples(): Int = impl.totalExamples()

  def inputColumns(): Int = impl.inputColumns()

  def totalOutcomes(): Int = impl.totalOutcomes()

  def resetSupported(): Boolean = impl.resetSupported()

  def asyncSupported(): Boolean = impl.asyncSupported()

  def reset(): Unit = impl.reset()

  def batch(): Int = impl.batch()

  def cursor(): Int = impl.cursor()

  def numExamples(): Int = impl.numExamples()

  def setPreProcessor(preProcessor: DataSetPreProcessor): Unit = impl.setPreProcessor(preProcessor)

  def getPreProcessor: DataSetPreProcessor = impl.getPreProcessor

  def getLabels: util.List[String] = impl.getLabels

  def hasNext: Boolean = impl.hasNext

  def next(): DataSet = impl.next()

  def reshuffle(rng: Random): Unit = {
    val coll = rng.shuffle(data.toSeq).asJava
    synchronized(underlyingIter = new ListDataSetIterator(coll, batchSize))
  }
}
