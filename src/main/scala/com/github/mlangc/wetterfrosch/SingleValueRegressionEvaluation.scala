package com.github.mlangc.wetterfrosch

trait SingleValueRegressionEvaluation {
  def mse: Double

  override def toString: String = {
    Array("mse" -> mse)
      .map(entry => entry._1 + " -> " + entry._2)
      .mkString("Evaluation(", ", ", ")")
  }
}
