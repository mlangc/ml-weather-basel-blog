package com.github.mlangc.wetterfrosch.dl4j

import java.io.{ByteArrayInputStream, ByteArrayOutputStream}
import java.nio.ByteBuffer

import com.github.mlangc.wetterfrosch.util.store.Pickler
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork
import org.deeplearning4j.util.ModelSerializer

class Dl4jMultiLayerNetowrkPickler extends Pickler[MultiLayerNetwork] {
  def toBytes(model: MultiLayerNetwork): ByteBuffer = {
    val bout = new ByteArrayOutputStream()
    ModelSerializer.writeModel(model, bout, true)
    ByteBuffer.wrap(bout.toByteArray)
  }

  def fromBytes(bytes: ByteBuffer): MultiLayerNetwork = {
    val bin = new ByteArrayInputStream(bytes.array())
    ModelSerializer.restoreMultiLayerNetwork(bin, true)
  }
}
