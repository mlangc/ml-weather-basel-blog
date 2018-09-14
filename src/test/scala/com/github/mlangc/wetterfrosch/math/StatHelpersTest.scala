package com.github.mlangc.wetterfrosch.math

import org.scalatest.FreeSpec

import scala.util.Random

class StatHelpersTest extends FreeSpec {
  "simple data" in {
    val vs = Random.shuffle(Seq(1.0, 2.0, 2.0, 3.0, 4.0)).toArray
    assert(StatHelpers.mean(vs) == 2.4)
    assert(StatHelpers.median(vs) == 2.0)
  }
}
