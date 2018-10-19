package com.github.mlangc.wetterfrosch.neuro.flow

import com.github.mlangc.wetterfrosch.{DeriveSingleElemPredictor, SingleValuePredictor}
import neuroflow.nets.cpu.DenseNetworkDouble

class NeuroFlowFfNnSingleValuePredictor(network: DenseNetworkDouble,
                                        val targetCol: String,
                                        featuresExtractor: NeuroFlowFeaturesExtractor = DefaultNeuroFlowFeaturesExtractor)
  extends SingleValuePredictor with DeriveSingleElemPredictor {

  def predict(seqs: Seq[Seq[Map[String, Double]]]): Seq[Double] = {
    network.batchApply(featuresExtractor.toFeatures(seqs))
      .flatMap(_.toArray)
  }
}
