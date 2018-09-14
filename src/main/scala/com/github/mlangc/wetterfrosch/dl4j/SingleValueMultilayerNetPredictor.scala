package com.github.mlangc.wetterfrosch.dl4j

import com.github.mlangc.wetterfrosch.{DeriveSingleElemPredictor, SingleValuePredictor}
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator

class SingleValueMultilayerNetPredictor(net: MultiLayerNetwork,
                                        val targetCol: String,
                                        toDataSetIter: Seq[Seq[Map[String, Double]]] => DataSetIterator)
  extends SingleValuePredictor with DeriveSingleElemPredictor {

  def predict(seqs: Seq[Seq[Map[String, Double]]]): Seq[Double] = {
    val out = net.output(toDataSetIter(seqs), false)

    0.until(seqs.size).map { i =>
      out.getDouble(i, 0, 0)
    }
  }
}
