package com.github.mlangc.wetterfrosch.dl4j

import com.github.mlangc.wetterfrosch.{DeriveSingleElemPredictor, SingleValuePredictor}
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork

class Dl4jFfNnSingleValuePredictor(ffNn: MultiLayerNetwork,
                                   val targetCol: String,
                                   featuresExtractor: Dl4jFeaturesExtractor) extends SingleValuePredictor with DeriveSingleElemPredictor {

  def predict(seqs: Seq[Seq[Map[String, Double]]]): Seq[Double] = {
    val nnOut = ffNn.output(featuresExtractor.toFeatures(seqs), false)
    seqs.indices.map(nnOut.getDouble)
  }
}
