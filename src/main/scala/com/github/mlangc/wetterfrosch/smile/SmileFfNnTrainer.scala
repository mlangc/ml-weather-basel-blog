package com.github.mlangc.wetterfrosch.smile

import smile.regression.NeuralNetwork.ActivationFunction
import smile.regression.{NeuralNetwork, Regression}

class SmileFfNnTrainer(override val featuresExtractor: SmileFeaturesExtractor = DefaultSmileFeaturesExtractor) extends
  AbstractSmileRegressionTrainer((features: Array[Array[Double]], labels: Array[Double]) => {
    val inputUnits = features.head.size
    val trainer = new NeuralNetwork.Trainer(ActivationFunction.TANH, inputUnits, inputUnits / 3, 1)
    trainer.setNumEpochs(500*16)
    trainer.setLearningRate(0.01)
    trainer.train(features, labels)
  })
