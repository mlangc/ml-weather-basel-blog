package com.github.mlangc.wetterfrosch.smile

import smile.regression.NeuralNetwork
import smile.regression.NeuralNetwork.ActivationFunction
import smile.validation

import scala.annotation.tailrec
import scala.collection.parallel.ParSeq
import scala.math.max
import scala.math.min
import scala.math.exp
import scala.util.Random

object TuneFfNn extends SmileLabModule {
  object cfg {
    def epochs = 1
    def nEvaluations = 50
  }

  def main(args: Array[String]): Unit = {
    println(s"Using ${cfg.epochs} epochs for training with ${cfg.nEvaluations} evaluations for each setting...")
    println()

    val evaluations = ParSeq(NnSettings(hiddenUnits = 3), NnSettings(hiddenUnits = 5), NnSettings(hiddenUnits = 10), NnSettings(hiddenUnits = 15), NnSettings(hiddenUnits = 20))
      .map(s => eval(s) -> s)
      .seq
      .sortBy(_._1)

    evaluations.foreach { case (mse, settings) =>
        println(f"$mse%10.2f - $settings")
    }

    println("Exploring hidden units and learning rate")

    val explorations = exploreNumHiddenUnitsAndLearningRate()
    explorations.foreach { case (mse, settings) =>
      println(f"$mse%10.2f - $settings")
    }
  }

  private def nInput = trainFeatures(0).length

  private case class NnSettings(activationFunction: ActivationFunction = ActivationFunction.LOGISTIC_SIGMOID,
                                hiddenUnits: Int = 4, learningRate: Double = 5e-4, momentum: Double = 0.0,
                                weightDecay: Double = 0.0)

  private def exploreNumHiddenUnitsAndLearningRate(): Seq[(Double, NnSettings)] = {
    @tailrec
    def explore(explored: Map[NnSettings, Double], retry: Int = 0): Map[NnSettings, Double] = {
      if (explored.size >= 15) explored else {
        val (bestSetting, bestRmse) = explored.toSeq.minBy(_._2)

        val settingsToTry = Random.nextInt(4) match {
          case 0 =>
            val hiddenUnitsToTry = max(2, bestSetting.hiddenUnits - 5).to(bestSetting.hiddenUnits + 5)
            hiddenUnitsToTry.map(u => bestSetting.copy(hiddenUnits = u))

          case 1 =>
            val learningRatesRange = (bestSetting.learningRate * 2 - bestSetting.learningRate / 2)
            val steps = 10
            val step = learningRatesRange/(steps - 1)
            val learningRatesToTry = 0.to(steps - 1).map(i => bestSetting.learningRate/2 + step*i)
            learningRatesToTry.map(r => bestSetting.copy(learningRate = r))

          case 2 =>
            val weightDecaysToTry = valuesBetweenZeroAndOneToTry(bestSetting.weightDecay, 10).map(_ * 0.1)
            weightDecaysToTry.map(d => bestSetting.copy(weightDecay = d))

          case _ =>
            val momentumsToTry = valuesBetweenZeroAndOneToTry(bestSetting.momentum, 10)
            momentumsToTry.map(m => bestSetting.copy(momentum = m))
        }

        val evaluations = settingsToTry.par.map(s => s -> eval(s))
        val (bestTunedSettings, bestTunedRmse) = evaluations.minBy(_._2)

        if (bestTunedRmse < bestRmse) {
          explore(explored + (bestTunedSettings -> bestTunedRmse), retry)
        } else {
          if (retry > 5) explored
          else explore(explored + (bestTunedSettings -> bestTunedRmse), retry + 1)
        }
      }
    }

    explore(Map(NnSettings() -> eval(NnSettings()))).toSeq
      .map(_._1)
      .par
      .map(s => eval(s) -> s)
      .seq
      .sortBy(-_._1)
  }

  private def valuesBetweenZeroAndOneToTry(actual: Double, n: Int): Seq[Double] = {
    require(n > 1)

    if (actual == 0.0) {
      actual +: (-n + 1).to(-1).map(y => exp(y))
    } else if (actual == 1.0) {
      valuesBetweenZeroAndOneToTry(0, n).map(1.0 - _)
    } else if (actual > 0.0 && actual < 1.0) {
      val left = actual/2
      val right = actual + (1 - actual)/2
      val step = (right - left)/(n - 1)
      0.to(n-1).map(i => left + step*i)
    } else {
      throw new IllegalArgumentException("" + actual)
    }
  }

  private def eval(settings: NnSettings): Double = {
    val trainer = new NeuralNetwork.Trainer(settings.activationFunction, nInput, settings.hiddenUnits, 1)
    trainer.setNumEpochs(cfg.epochs)
    trainer.setLearningRate(settings.learningRate)
    trainer.setMomentum(0.1)
    trainer.setWeightDecay(settings.weightDecay)

    1.to(cfg.nEvaluations).map { _ =>
      val nn = trainer.train(trainFeatures, trainLabels)
      val trainPredictions = nn.predict(trainFeatures)
      validation.rmse(trainLabels, trainPredictions)
    }.sum / cfg.nEvaluations
  }
}
