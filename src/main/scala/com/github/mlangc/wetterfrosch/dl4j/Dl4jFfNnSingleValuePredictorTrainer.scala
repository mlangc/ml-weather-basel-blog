package com.github.mlangc.wetterfrosch.dl4j

import com.github.mlangc.wetterfrosch.{SingleValuePredictor, SingleValuePredictorTrainer}
import org.deeplearning4j.nn.api.OptimizationAlgorithm
import org.deeplearning4j.nn.conf.layers.{DenseLayer, OutputLayer}
import org.deeplearning4j.nn.conf.{MultiLayerConfiguration, NeuralNetConfiguration}
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork
import org.deeplearning4j.nn.weights.WeightInit
import org.deeplearning4j.optimize.listeners.ScoreIterationListener
import org.nd4j.linalg.activations.Activation
import org.nd4j.linalg.learning.config.{Adam, Sgd}
import org.nd4j.linalg.lossfunctions.LossFunctions

class Dl4jFfNnSingleValuePredictorTrainer(seed: Int,
                                          batchSize: Int = Dl4jDefaults.batchSize,
                                          epochs: Int = 2,
                                          featuresExtractor: Dl4jFeaturesExtractor = DefaultDl4jFfNnFeaturesExtractor) extends SingleValuePredictorTrainer {

  def train(trainingData: Seq[Seq[Map[String, Double]]], targetCol: String): SingleValuePredictor = {
    val numInput = trainingData.head.init.map(_.size).sum
    val numOutput = 1
    val numHidden1 = 4

    val nnConf: MultiLayerConfiguration = new NeuralNetConfiguration.Builder()
      .seed(seed)
      .weightInit(WeightInit.XAVIER)
      .optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
      .list()
      .layer(0, new DenseLayer.Builder()
        .nIn(numInput).nOut(numHidden1)
        .activation(Activation.LEAKYRELU).build())
      .layer(1, new OutputLayer.Builder(LossFunctions.LossFunction.MSE)
        .nIn(numHidden1).nOut(numOutput)
        .activation(Activation.IDENTITY).build())
      .pretrain(false)
      .backprop(true)
      .build()

    val nn = new MultiLayerNetwork(nnConf)
    nn.init()
    nn.setListeners(new ScoreIterationListener(1))

    val trainingIter = featuresExtractor.toFeaturesWithLabels(trainingData, targetCol, batchSize)
    1.to(5).foreach { epoch =>
      nn.fit(trainingIter)
      trainingIter.reset()
    }

    new Dl4jFfNnSingleValuePredictor(nn, targetCol, featuresExtractor)
  }
}
