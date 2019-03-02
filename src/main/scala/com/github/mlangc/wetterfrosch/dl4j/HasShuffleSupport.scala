package com.github.mlangc.wetterfrosch.dl4j

import org.nd4j.linalg.dataset.api.iterator.DataSetIterator

import scala.util.Random

trait HasShuffleSupport { this: DataSetIterator =>
  def reshuffle(rng: Random): Unit
}
