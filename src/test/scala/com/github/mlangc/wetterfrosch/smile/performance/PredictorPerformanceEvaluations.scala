package com.github.mlangc.wetterfrosch.smile.performance

import com.github.mlangc.wetterfrosch.smile.implicits.smileRegressionPickler
import com.github.mlangc.wetterfrosch.util.store.StoreKey
import org.scalameter.Gen
import org.scalameter.picklers.noPickler._
import smile.regression
import smile.regression.{GradientTreeBoost, Regression}


object PredictorPerformanceEvaluations extends SmileLocalBench {
  override def timeSeriesLen: Int = 2

  private type SmileRegression = Regression[Array[Double]] with java.io.Serializable
  private case class Predictor(name: String, regression: SmileRegression) {
    override def toString: String = {
      name
    }
  }

  private object Predictor {
    def fromTrainer(basename: String, trainer: (Array[Array[Double]], Array[Double]) => SmileRegression): Predictor = {
      val name = expandName(basename)
      val key = StoreKey(getClass, name + "-regression")
      Predictor(expandName(basename), objectStore.load(key)(trainer(trainFeatures, trainLabels)))
    }
  }

  private val predictors = Gen.enumeration("predictors")(
    Predictor.fromTrainer("OLS", regression.ols(_, _)),
    Predictor.fromTrainer("Tree", regression.cart(_, _, 11)),
    Predictor.fromTrainer("Forest", regression.randomForest(_, _)),
    Predictor.fromTrainer("GBM", regression.gbm(_, _, loss = GradientTreeBoost.Loss.LeastSquares))
  )

  performance of "predictors" in {
    measure method "predict" in {
      using(predictors) in { predictor =>
        predictor.regression.predict(testFeatures)
      }
    }
  }
}
