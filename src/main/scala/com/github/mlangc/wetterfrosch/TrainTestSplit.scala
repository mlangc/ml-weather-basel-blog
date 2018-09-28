package com.github.mlangc.wetterfrosch

import scala.util.Random


class TrainTestSplit(labeledData: Seq[Seq[Map[String, Double]]],
                     seed: Int = 0) {

  lazy val (trainingData: Seq[Seq[Map[String, Double]]], testData: Seq[Seq[Map[String, Double]]]) = {

    def forTraining(elem: (_, Int)) = elem._2 % 5 != 0

    def forTest(elem: (_, Int)) = !forTraining(elem)

    val rng = new Random(seed)
    val shuffledData = rng.shuffle(labeledData)

    val train: Seq[Seq[Map[String, Double]]] = shuffledData
      .zipWithIndex.filter(forTraining).map(_._1)
    val test: Seq[Seq[Map[String, Double]]] = shuffledData
      .zipWithIndex.filter(forTest).map(_._1)

    (train, test)
  }
}
