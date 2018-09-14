package com.github.mlangc.wetterfrosch

import scala.math._

trait SingleValueRegressionEvaluation {
  def mae: Double
  def mse: Double
  def rmse: Double  = sqrt(mse)

  override def toString: String = {
    Array("rmse" -> rmse, "mae" -> mae)
      .map(entry => entry._1 + " -> " + entry._2)
      .mkString("Evaluation(", ", ", ")")
  }
}
