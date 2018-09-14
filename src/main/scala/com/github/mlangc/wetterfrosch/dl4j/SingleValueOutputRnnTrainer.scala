package com.github.mlangc.wetterfrosch.dl4j

import com.github.mlangc.wetterfrosch.SingleValuePredictor
import com.github.mlangc.wetterfrosch.SingleValuePredictorTrainer
import com.typesafe.scalalogging.StrictLogging
import org.deeplearning4j.nn.api.OptimizationAlgorithm
import org.deeplearning4j.nn.conf.NeuralNetConfiguration
import org.deeplearning4j.nn.conf.layers.LSTM
import org.deeplearning4j.nn.conf.layers.RnnOutputLayer
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork
import org.deeplearning4j.nn.weights.WeightInit
import org.deeplearning4j.optimize.listeners.ScoreIterationListener
import org.nd4j.linalg.activations.Activation
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator
import org.nd4j.linalg.learning.config.Adam
import org.nd4j.linalg.lossfunctions.LossFunctions.LossFunction

class SingleValueOutputRnnTrainer(seed: Int, batchSize: Int) extends SingleValuePredictorTrainer with StrictLogging {
  def train(trainingData: Seq[Seq[Map[String, Double]]], targetCol: String): SingleValuePredictor = {
    val nFeatures = trainingData.head.head.keySet.size
    val net: MultiLayerNetwork = trainRnn(new ExportDataIter(trainingData, true, batchSize), 2, nFeatures)
    return new SingleValueMultilayerNetPredictor(net, targetCol, data => new ExportDataIter(data, false, batchSize))
  }

  private def initRnn(nFeatures: Int) = {
    val conf = new NeuralNetConfiguration.Builder()
      .optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
      .weightInit(WeightInit.XAVIER)
      .updater(new Adam())
      .iterations(1)
      .seed(seed)
      .list()
      .layer(0, new LSTM.Builder().name("lstm1")
        .activation(Activation.TANH).nIn(nFeatures).nOut(8).build())
      .layer(1, new RnnOutputLayer.Builder().name("output")
        .activation(Activation.IDENTITY)
        .lossFunction(LossFunction.MSE)
        .nIn(8).nOut(1).build())
      .pretrain(false)
      .backprop(true)
      .build()

    val net = new MultiLayerNetwork(conf)
    net.init()
    net.setListeners(new ScoreIterationListener(1))
    net
  }

  private def trainRnn(trainIter: DataSetIterator, epochs: Int, nFeatures: Int) = {
    val net = initRnn(nFeatures)

    for (epoch <- 1.to(epochs)) {
      logger.debug(s"Entering epoch $epoch")
      net.fit(trainIter)
      trainIter.reset()
    }

    net
  }
}
