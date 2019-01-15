package com.github.mlangc.wetterfrosch.dl4j


import com.github.mlangc.wetterfrosch.HistoryExportColSubsets
import com.github.mlangc.wetterfrosch.dl4j.implicits.dl4jMultiLayerNetworkPickler
import com.github.mlangc.wetterfrosch.util.store.StoreKey
import org.deeplearning4j.nn.api.OptimizationAlgorithm
import org.deeplearning4j.nn.conf.layers.{DenseLayer, OutputLayer}
import org.deeplearning4j.nn.conf.{MultiLayerConfiguration, NeuralNetConfiguration}
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork
import org.deeplearning4j.nn.weights.WeightInit
import org.deeplearning4j.optimize.listeners.ScoreIterationListener
import org.nd4j.linalg.activations.Activation
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator
import org.nd4j.linalg.learning.config.Adam
import org.nd4j.linalg.lossfunctions.LossFunctions

import scala.util.Random

object TrainAndEvalSimpleFfNn extends Dl4jLabModule {
  override def timeSeriesLen = 1
  def batchSize = 1024
  def maxTrainingExamples = 1000000
  def maxTestExamples = 1024
  def selectedCols: Set[String] = HistoryExportColSubsets.ColsFromLastDayForTree23

  override lazy val featuresExtractor = new SelectedColsDl4jFfNnFeaturesExtractor(selectedCols)

  def main(args: Array[String]): Unit = {
    val numInput = selectedCols.size * timeSeriesLen
    val numOutput = 1
    val numHidden1 = 8
    val modelKey = StoreKey(getClass, "ffNn-3")

    def trainModelInitialModel() = {
      val nnConf: MultiLayerConfiguration = new NeuralNetConfiguration.Builder()
        .seed(seed)
        .weightInit(WeightInit.UNIFORM)
        .optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
        .updater(new Adam())
        .list()
        .layer(0, new DenseLayer.Builder()
          .nIn(numInput).nOut(numHidden1)
          .activation(Activation.LEAKYRELU).build())
        .layer(1, new OutputLayer.Builder(LossFunctions.LossFunction.MEAN_SQUARED_LOGARITHMIC_ERROR)
          .nIn(numHidden1).nOut(numOutput)
          .activation(Activation.IDENTITY).build())
        .pretrain(false)
        .backprop(true)
        .build()

      val nn = new MultiLayerNetwork(nnConf)
      nn.init()
      nn.setListeners(new ScoreIterationListener(1))
      trainNn(nn)
    }

    def trainNn(nn: MultiLayerNetwork): MultiLayerNetwork = {
      val trainingIter = getTrainingIter(batchSize)
      1.to(50000).foreach { epoch =>
        nn.fit(trainingIter)
        trainingIter.reset()

        val testIter = getTestIter(batchSize)
        val testEvaluation = nn.evaluateRegression(testIter)

        /*
        println(s"Test error (epoch $epoch): ")
        println(s"  RMSE: ${testEvaluation.averagerootMeanSquaredError()}")
        println(s"  MAE: ${testEvaluation.averageMeanAbsoluteError()}")
        */

        val trainEvaluation = nn.evaluateRegression(trainingIter)
        trainingIter.reset()

        println(s"Training error (epoch $epoch): ")
        println(s"  RMSE: ${trainEvaluation.averagerootMeanSquaredError()}")
        println(s"  MAE: ${trainEvaluation.averageMeanAbsoluteError()}")
        println()
      }

      nn
    }

    def continueTraining(nn: MultiLayerNetwork): MultiLayerNetwork = {
      var trainingIter = getTrainingIter(batchSize)

      1.to(5000).foreach { epoch =>
        nn.fit(trainingIter)
        trainingIter.reset()

        val testIter = getTestIter(batchSize)
        val testEvaluation = nn.evaluateRegression(testIter)

        println(s"Test error (epoch $epoch): ")
        println(s"  RMSE: ${testEvaluation.averagerootMeanSquaredError()}")
        println(s"  MAE: ${testEvaluation.averageMeanAbsoluteError()}")

        val trainEvaluation = nn.evaluateRegression(trainingIter)
        trainingIter = getTestIter(batchSize)

        println(s"Training error (epoch $epoch): ")
        println(s"  RMSE: ${trainEvaluation.averagerootMeanSquaredError()}")
        println(s"  MAE: ${trainEvaluation.averageMeanAbsoluteError()}")
        println()
      }

      nn
    }

    val nn = objectStore.load(modelKey)(trainModelInitialModel())
    continueTraining(nn)
  }

  private def getTrainingIter(batchSize: Int): DataSetIterator = {
    toLabeledDataSetIter(Random.shuffle(trainTestSplit.trainingData).take(maxTrainingExamples), batchSize)
  }

  private def getTestIter(batchSize: Int): DataSetIterator = {
    toLabeledDataSetIter(trainTestSplit.testData.take(maxTestExamples), batchSize)
  }

  private def toLabeledDataSetIter(labeledData: Seq[Seq[Map[String, Double]]], batchSize: Int): DataSetIterator = {
    featuresExtractor.toFeaturesWithLabels(labeledData, targetCol, batchSize)
  }
}
