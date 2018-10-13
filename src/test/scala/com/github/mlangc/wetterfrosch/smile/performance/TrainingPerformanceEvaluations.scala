package com.github.mlangc.wetterfrosch.smile.performance

import org.scalameter._
import org.scalameter.picklers.noPickler._
import smile.regression
import smile.regression.Regression

object TrainingPerformanceEvaluations extends SmileLocalBench {
  override def timeSeriesLen: Int = 2

  private type TrainingFun = (Array[Array[Double]], Array[Double]) => Regression[Array[Double]]

  private case class Trainer(name: String, fun: TrainingFun) {
    override def toString: String = {
      s"${getClass.getSimpleName}[$name]"
    }
  }

  private object Trainer {
    def withBasename(basename: String, fun: TrainingFun): Trainer = {
      Trainer(expandName(basename), fun)
    }
  }

  private val trainers: Gen[Trainer] = Gen.enumeration("trainer")(
    Trainer.withBasename("OLS", regression.ols(_, _)),
    Trainer.withBasename("Tree", regression.cart(_, _, 23)),
    Trainer.withBasename("Forest", regression.randomForest(_, _)),
    Trainer.withBasename("GBM", regression.gbm(_, _))
  )

  performance of "trainers" in {
    measure method "train" in {
      using(trainers) in { trainer =>
        trainer.fun(trainFeatures, trainLabels)
      }
    }
  }
}
