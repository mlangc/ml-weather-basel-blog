package com.github.mlangc.wetterfrosch.dl4j


import com.github.mlangc.wetterfrosch.HistoryExportColSubsets
import com.github.mlangc.wetterfrosch.dl4j.implicits.dl4jMultiLayerNetworkPickler
import com.github.mlangc.wetterfrosch.util.store.StoreKey
import org.deeplearning4j.nn.api.OptimizationAlgorithm
import org.deeplearning4j.nn.conf.layers.{DenseLayer, OutputLayer}
import org.deeplearning4j.nn.conf.{LearningRatePolicy, MultiLayerConfiguration, NeuralNetConfiguration}
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork
import org.deeplearning4j.nn.weights.WeightInit
import org.deeplearning4j.optimize.listeners.ScoreIterationListener
import org.nd4j.linalg.activations.Activation
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator
import org.nd4j.linalg.learning.config.{Adam, RmsProp}
import org.nd4j.linalg.lossfunctions.LossFunctions

import scala.util.Random

object TrainAndEvalSimpleFfNn extends Dl4jLabModule {
  override def timeSeriesLen = 1
  private def batchSize = 128
  private def maxTrainingExamples = Int.MaxValue
  private def maxTestExamples = Int.MaxValue
  private def selectedCols: Set[String] = HistoryExportColSubsets.ColsFromLastDayForTree23
  private val rng = new Random(seed)

  override lazy val featuresExtractor = new SelectedColsDl4jFfNnFeaturesExtractor(selectedCols)

  def main(args: Array[String]): Unit = {
    val numInput = selectedCols.size * timeSeriesLen
    val numOutput = 1
    val numHidden = 8
    val learningRate = 3.75e-01
    val modelKey = StoreKey(getClass, "ffNn-16")

    def trainModelInitialModel() = {
val nnConf: MultiLayerConfiguration = new NeuralNetConfiguration.Builder()
  .seed(seed)
  .weightInit(WeightInit.XAVIER)
  .optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
  //.updater(new Adam(learningRate, Adam.DEFAULT_ADAM_BETA1_MEAN_DECAY, Adam.DEFAULT_ADAM_BETA2_VAR_DECAY, Adam.DEFAULT_ADAM_EPSILON))
  .updater(new RmsProp(learningRate, RmsProp.DEFAULT_RMSPROP_RMSDECAY, RmsProp.DEFAULT_RMSPROP_EPSILON))
  .learningRate(learningRate)
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
      trainNn(nn)
    }

    def trainNn(nn: MultiLayerNetwork): MultiLayerNetwork = {
      val trainingIter = getTrainingIter(batchSize)
      1.to(1).foreach { epoch =>
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
      val trainingIter = getTrainingIter(batchSize)

1.to(500).foreach { epoch =>
  nn.fit(trainingIter)
  trainingIter.reset()

//        val testIter = getTestIter(batchSize)
//        val testEvaluation = nn.evaluateRegression(testIter)
//
//        println(s"Test error (epoch $epoch): ")
//        println(s"  RMSE: ${testEvaluation.averagerootMeanSquaredError()}")
//        println(s"  MAE: ${testEvaluation.averageMeanAbsoluteError()}")

        val trainEvaluation = nn.evaluateRegression(trainingIter)
        trainingIter.reshuffle(rng)

        println(s"Training error (epoch $epoch): ")
        println(s"  RMSE: ${trainEvaluation.averagerootMeanSquaredError()}")
        println(s"  MAE: ${trainEvaluation.averageMeanAbsoluteError()}")
        println()
      }

      nn
    }

    while (true) {
      val nn = continueTraining(objectStore.load(modelKey)(trainModelInitialModel()))
      objectStore.put(modelKey, nn)
    }
  }

  private def getTrainingIter(batchSize: Int): DataSetIterator with HasShuffleSupport = {
    toLabeledDataSetIter(rng.shuffle(trainTestSplit.trainingData).take(maxTrainingExamples), batchSize)
  }

  private def getTestIter(batchSize: Int): DataSetIterator with HasShuffleSupport = {
    toLabeledDataSetIter(trainTestSplit.testData.take(maxTestExamples), batchSize)
  }

  private def toLabeledDataSetIter(labeledData: Seq[Seq[Map[String, Double]]], batchSize: Int): DataSetIterator with HasShuffleSupport = {
    featuresExtractor.toFeaturesWithLabels(labeledData, targetCol, batchSize)
  }
}
