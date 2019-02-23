package com.github.mlangc.wetterfrosch.smile.snippets

import smile.regression.NeuralNetwork
import smile.regression.NeuralNetwork.ActivationFunction

object TrainSmileFfNn {
val trainFeatures: Array[Array[Double]] = ???
val trainLabels: Array[Double] = ???
val inputUnits = trainFeatures.head.length
val hiddenUnits = 4
val outputUnits = 1

val trainer = new NeuralNetwork.Trainer(
  ActivationFunction.LOGISTIC_SIGMOID,
  inputUnits,
  hiddenUnits,
  1)

trainer.setNumEpochs(5)
trainer.setLearningRate(5e-4)
val network = trainer.train(trainFeatures, trainLabels)
}
