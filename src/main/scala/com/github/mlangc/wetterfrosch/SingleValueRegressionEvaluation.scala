package com.github.mlangc.wetterfrosch

import com.github.mlangc.wetterfrosch.math.StatHelpers

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

object SingleValueRegressionEvaluation {
  def mean(evaluations: Seq[SingleValueRegressionEvaluation]): SingleValueRegressionEvaluation = {
    new SingleValueRegressionEvaluation {
      val mae: Double = StatHelpers.mean(evaluations.map(_.mae))
      val mse: Double = StatHelpers.mean(evaluations.map(_.mse))
    }
  }
}
