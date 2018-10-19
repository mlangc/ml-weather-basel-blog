package com.github.mlangc.wetterfrosch.neuro.flow

import com.github.mlangc.wetterfrosch.{ExportDataUtils, LabModule}
import neuroflow.core.Activators.Double._
import neuroflow.core._
import neuroflow.dsl._
import neuroflow.nets.cpu.DenseNetwork._
import neuroflow.core.Network.Vectors
import breeze.linalg.DenseVector
import neuroflow.nets.cpu.DenseNetworkDouble

object EvalSimpleFfNn extends NeuroFlowLabModule {
  def main(args: Array[String]): Unit = {
    implicit val weights: WeightBreeder[Double] = WeightBreeder[Double].normal(0.0, 1.0)
    val net: DenseNetworkDouble = Network(
      layout =
        Vector(colNames.size) ::
        Dense(20, ReLU) ::
        Dense(1, Linear) ::
        SquaredError(),
      settings = Settings[Double](
        updateRule = Vanilla(),
        batchSize = Some(1024),
      )
    )

    val (features, labels) = toFeaturesWithLabels(trainTestSplit.trainingData)
    net.train(features, labels)

    val predictor = new NeuroFlowFfNnSingleValuePredictor(net, targetCol)
    val trainEvaluation = evaluator.eval(predictor, trainTestSplit.trainingData)
    val testEvaluation = evaluator.eval(predictor, trainTestSplit.testData)

    println(s"Train: $trainEvaluation")
    println(s"Test : $testEvaluation")
  }

  private def toFeaturesWithLabels(seqs: Seq[Seq[Map[String, Double]]]): (Vectors[Double], Vectors[Double]) = {
    seqs.map { seq =>
      val features = DenseVector(ExportDataUtils.toDoubles(seq.init): _*)
      val label = DenseVector(seq.last(targetCol))

      (features, label)
    }.unzip
  }

}
