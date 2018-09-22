package com.github.mlangc.wetterfrosch.smile

import com.github.mlangc.wetterfrosch.math.StatHelpers
import com.typesafe.scalalogging.StrictLogging
import smile.regression
import smile.validation.RMSE
import smile.validation.cv

import scala.math._

object EvaluateNormalization extends SmileLabModule with StrictLogging {
  def main(args: Array[String]): Unit = {
    val featuresNormalized = normalize(trainFeatures)

    val folds = 20
    val nFolds = 30

    val results: Seq[(Double, Double)] = Seq(trainFeatures, featuresNormalized).map { features =>
      val rmses = 1.to(folds).map { _ =>
        cv(features, trainLabels, nFolds, new RMSE())(regression.ols(_, _))(0)
      }

      val z = 1.96
      StatHelpers.meanAndSd(rmses.toArray)
    }

    val (diff, se) = results.reduce { (l, r)  =>
      val (m1, s1) = l
      val (m2, s2) = r
      (m1 - m2, sqrt(pow(s1, 2)/nFolds + pow(s2, 2)/nFolds))
    }

    logger.info(s"$diff +- ${2*se}")
  }

  private def normalize(features: Array[Array[Double]]): Array[Array[Double]] = {
    val scale: Array[Double] = features.reduce { (left, right) =>
      left.zip(right).map { case (l, r) =>
        val al = abs(l)
        val ar = abs(r)
        if (al > ar) al else ar
      }
    }

    features.map { features =>
      features.zip(scale).map { case (f, s) =>
        if (s == 0) 0.0 else f/s
      }
    }
  }
}
