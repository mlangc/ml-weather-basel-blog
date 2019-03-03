package com.github.mlangc.wetterfrosch.util

import scala.util.Random

trait RandomModule {
  def seed: Int = 42
  lazy val rng = new Random(seed)
}
