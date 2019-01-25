package com.github.mlangc.wetterfrosch.smile

import smile.regression.NeuralNetwork
import smile.regression.NeuralNetwork.ActivationFunction
import smile.validation

import scala.annotation.tailrec
import scala.collection.parallel.ParSeq
import scala.math.max
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
                                hiddenUnits: Int = 2, learningRate: Double = 5e-4, momentum: Double = 0.9)

  private def exploreNumHiddenUnitsAndLearningRate(): Seq[(Double, NnSettings)] = {
    @tailrec
    def explore(explored: Map[NnSettings, Double]): Map[NnSettings, Double] = {
      if (explored.size >= 15) explored else {
        val (bestSetting, bestRmse) = explored.toSeq.minBy(_._2)

        val tuneHiddenUnits = Random.nextBoolean()

        val settingsToTry = if (tuneHiddenUnits) {
          val hiddenUnitsToTry = max(2, bestSetting.hiddenUnits - 5).to(bestSetting.hiddenUnits + 5)
          hiddenUnitsToTry.map(u => bestSetting.copy(hiddenUnits = u))
        } else {
          val learningRatesRange = (bestSetting.learningRate*2 - bestSetting.learningRate / 2)
          val steps = 10
          val step = learningRatesRange/(steps - 1)
          val learningRatesToTry = 0.to(steps - 1).map(i => bestSetting.learningRate/2 + step*i)
          learningRatesToTry.map(r => bestSetting.copy(learningRate = r))
        }

        val evaluations = settingsToTry.par.map(s => s -> eval(s))
        val (bestTunedSettings, bestTunedRmse) = evaluations.minBy(_._2)

        if (bestTunedRmse < bestRmse) explore(explored + (bestTunedSettings -> bestTunedRmse))
        else explored
      }
    }

    explore(Map(NnSettings() -> eval(NnSettings()))).toSeq
      .map(_._1)
      .par
      .map(s => eval(s) -> s)
      .seq
      .sortBy(-_._1)
  }

  private def eval(settings: NnSettings): Double = {
    val trainer = new NeuralNetwork.Trainer(settings.activationFunction, nInput, settings.hiddenUnits, 1)
    trainer.setNumEpochs(cfg.epochs)
    trainer.setLearningRate(settings.learningRate)
    trainer.setMomentum(0.1)

    1.to(cfg.nEvaluations).map { _ =>
      val nn = trainer.train(trainFeatures, trainLabels)
      val trainPredictions = nn.predict(trainFeatures)
      validation.rmse(trainLabels, trainPredictions)
    }.sum / cfg.nEvaluations
  }
}
