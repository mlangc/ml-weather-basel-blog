package com.github.mlangc.wetterfrosch.smile

import smile.regression.NeuralNetwork
import smile.regression.NeuralNetwork.ActivationFunction

import smile.validation

object ExperimentWithFfNn extends SmileLabModule {
  def main(args: Array[String]): Unit = {
    val trainingExamples = Int.MaxValue
    val epochs = 1000
    val actualTrainFeatures = trainFeatures.take(trainingExamples)
    val numInputs = actualTrainFeatures.head.size
    val actualTrainLabels = trainLabels.take(trainingExamples)

    val nn = new NeuralNetwork(ActivationFunction.LOGISTIC_SIGMOID, Array(numInputs, 2, 2, 1): _*)
    nn.setLearningRate(5e-4)
    //nn.setMomentum(0.9)
    //nn.setLearningRate(1e-6)

    0.until(epochs).foreach { epoch =>
      val rmse = validation.rmse(actualTrainLabels, nn.predict(actualTrainFeatures))
      println(f"RMSE at epoch $epoch: $rmse")
      nn.learn(actualTrainFeatures, actualTrainLabels)
    }
  }
}
