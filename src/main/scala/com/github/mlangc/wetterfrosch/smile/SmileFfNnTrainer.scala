package com.github.mlangc.wetterfrosch.smile

import smile.regression.NeuralNetwork.ActivationFunction
import smile.regression.{NeuralNetwork, Regression}

class SmileFfNnTrainer(override val featuresExtractor: SmileFeaturesExtractor = DefaultSmileFeaturesExtractor) extends
  AbstractSmileRegressionTrainer("Feed Forward Network",
      (features: Array[Array[Double]], labels: Array[Double]) => {
        val inputUnits = features.head.size
        val trainer = new NeuralNetwork.Trainer(ActivationFunction.LOGISTIC_SIGMOID, inputUnits, 4, 1)
        trainer.setNumEpochs(5)
        trainer.setLearningRate(5e-4)
        trainer.train(features, labels)
      })
