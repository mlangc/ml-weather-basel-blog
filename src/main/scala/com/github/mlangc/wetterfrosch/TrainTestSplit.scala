package com.github.mlangc.wetterfrosch

import scala.util.Random


class TrainTestSplit(timeSeriesLen: Int, csvData: Seq[Map[String, Double]], seed: Int = 0) {
  lazy val (trainingData: Seq[Seq[Map[String, Double]]], testData: Seq[Seq[Map[String, Double]]]) = {
    def forTraining(elem: (_, Int)) = elem._2 % 5 != 0
    def forTest(elem: (_, Int)) = !forTraining(elem)

    val rng = new Random(seed)
    val timeSeries = rng.shuffle(csvData.sliding(timeSeriesLen + 1, 1)).toArray

    val train: Array[Seq[Map[String, Double]]] = timeSeries.zipWithIndex.filter(forTraining).map(_._1)
    val test: Array[Seq[Map[String, Double]]] = timeSeries.zipWithIndex.filter(forTest).map(_._1)

    (train.toSeq, test.toSeq)
  }
}
