package com.github.mlangc.wetterfrosch.dl4j

import com.github.mlangc.wetterfrosch.{ExportDataModule, ExportDataUtils, TrainTestSplit}
import org.deeplearning4j.datasets.iterator.impl.ListDataSetIterator
import org.deeplearning4j.nn.api.OptimizationAlgorithm
import org.deeplearning4j.nn.conf.{MultiLayerConfiguration, NeuralNetConfiguration}
import org.deeplearning4j.nn.conf.layers.{DenseLayer, OutputLayer}
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork
import org.deeplearning4j.nn.weights.WeightInit
import org.deeplearning4j.optimize.listeners.ScoreIterationListener
import org.nd4j.linalg.activations.Activation
import org.nd4j.linalg.dataset.DataSet
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator
import org.nd4j.linalg.factory.Nd4j
import org.nd4j.linalg.learning.config.{Adam, RmsProp, Sgd}
import org.nd4j.linalg.lossfunctions.LossFunctions

object TrainAndEvalSimpleFfNn extends ExportDataModule {
  def timeSeriesLen = 1
  override def batchSize = 128

  def trainTestData: Seq[Seq[Map[String, Double]]] = {
    labeledDataAssembler.assemblyDailyData(1)
      .filter(labeledDataFilter)
  }

  lazy val trainTestSplit = new TrainTestSplit(trainTestData, seed)

  def main(args: Array[String]): Unit = {
    val numInput = trainTestSplit.trainingData.head.head.size * timeSeriesLen
    val numOutput = 1
    val numHidden = numInput / 2

    val nnConf: MultiLayerConfiguration = new NeuralNetConfiguration.Builder()
      .seed(seed)
      .weightInit(WeightInit.XAVIER)
      .optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
      .updater(new Sgd())
      .list()
      .layer(0, new DenseLayer.Builder()
        .nIn(numInput).nOut(numHidden)
        .activation(Activation.SIGMOID).build())
      .layer(1, new OutputLayer.Builder(LossFunctions.LossFunction.MSE)
        .nIn(numHidden).nOut(numOutput)
        .activation(Activation.IDENTITY).build())
      .pretrain(false)
      .backprop(true)
      .build()

    val nn = new MultiLayerNetwork(nnConf)
    nn.init()
    nn.setListeners(new ScoreIterationListener(1))

    val trainingIter = getTrainingIter(batchSize)
    1.to(5).foreach { epoch =>
      nn.fit(trainingIter)
      trainingIter.reset()

      val testIter = getTestIter(batchSize)
      val testEvaluation = nn.evaluateRegression(testIter)

      println(s"Test error (epoch $epoch): ")
      println(s"  RMSE: ${testEvaluation.averagerootMeanSquaredError()}")
      println(s"  MAE: ${testEvaluation.averageMeanAbsoluteError()}")

      val trainEvaluation = nn.evaluateRegression(trainingIter)
      trainingIter.reset()

      println(s"Training error (epoch $epoch): ")
      println(s"  RMSE: ${trainEvaluation.averagerootMeanSquaredError()}")
      println(s"  MAE: ${trainEvaluation.averageMeanAbsoluteError()}")
      println()
    }
  }

  private def getTrainingIter(batchSize: Int): DataSetIterator = {
    toLabeledDataSetIter(trainTestSplit.trainingData, batchSize)
  }

  private def getTestIter(batchSize: Int): DataSetIterator = {
    toLabeledDataSetIter(trainTestSplit.testData, batchSize)
  }

  private def toLabeledDataSetIter(labeledData: Seq[Seq[Map[String, Double]]], batchSize: Int): DataSetIterator = {
    val numExamples = labeledData.size
    val numFeatures = labeledData.head.init.map(_.keys.size).sum

    val featureArr = Nd4j.create(numExamples, numFeatures)
    val labelsArr = Nd4j.create(numExamples, 1)

    labeledData.zipWithIndex.foreach { case (seq, i) =>
      val label = seq.last(targetCol)
      labelsArr.putScalar(Array(i, 0), label)

      ExportDataUtils.toDoubles(seq.init).zipWithIndex.foreach { case (d, j) =>
        featureArr.putScalar(Array(i, j), d)
      }
    }

    new ListDataSetIterator(new DataSet(featureArr, labelsArr).asList(), batchSize)
  }

}
