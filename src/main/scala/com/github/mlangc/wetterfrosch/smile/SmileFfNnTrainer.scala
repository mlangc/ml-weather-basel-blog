package com.github.mlangc.wetterfrosch.smile

import smile.regression.{NeuralNetwork, Regression}

class SmileFfNnTrainer extends
  AbstractSmileRegressionTrainer((features: Array[Array[Double]], labels: Array[Double]) => {
    val inputUnits = features.head.size
    val trainer = new NeuralNetwork.Trainer(inputUnits, inputUnits / 2, 1)
    trainer.setNumEpochs(50)
    trainer.setLearningRate(0.01)
    trainer.train(features, labels)
  })
