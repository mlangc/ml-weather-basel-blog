package com.github.mlangc.wetterfrosch.smile.performance

import com.github.mlangc.wetterfrosch.smile.SmileLabModule
import org.scalameter._
import org.scalameter.picklers.noPickler._
import org.scalameter.reporting.LoggingReporter
import smile.regression
import smile.regression.Regression

object TrainingPerformanceEvaluations extends Bench.LocalTime with SmileLabModule {
  override def timeSeriesLen: Int = 2
  override def persistor: Persistor = Persistor.None
  override def reporter = new LoggingReporter[Double]
  override def aggregator: Aggregator[Double] = Aggregator.average
  override def measurer: Measurer[Double] = new Measurer.Default

  private type TrainingFun = (Array[Array[Double]], Array[Double]) => Regression[Array[Double]]

  private case class Trainer(name: String, fun: TrainingFun) {
    override def toString: String = {
      s"${getClass.getSimpleName}[$name]"
    }
  }

  private object Trainer {
    def withBasename(basename: String, fun: TrainingFun): Trainer = {
      def mkName(basename: String) = {
        val dailyHourly = if (useHourlyData) "hourly" else "daily"
        s"$basename-$dailyHourly-$timeSeriesLen"
      }

      Trainer(mkName(basename), fun)
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
