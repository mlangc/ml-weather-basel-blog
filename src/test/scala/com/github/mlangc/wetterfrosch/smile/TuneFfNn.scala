package com.github.mlangc.wetterfrosch.smile

import com.github.mlangc.wetterfrosch.util.tune.{SettingsTuner, TuningHelpers}
import smile.regression.NeuralNetwork
import smile.regression.NeuralNetwork.ActivationFunction
import smile.validation

import scala.math.max

object TuneFfNn extends SmileLabModule {
  object cfg {
    def epochs = 1
    def nEvaluations = 50
  }

  def main(args: Array[String]): Unit = {
    println(s"Using ${cfg.epochs} epochs for training with ${cfg.nEvaluations} evaluations for each setting...")
    println()
    println("Exploring different settings...")

    val explorations = exploreSettings()
    explorations.foreach { case (settings, mse) =>
      println(f"$mse%10.2f - $settings")
    }
  }

  private def nInput = trainFeatures(0).length

  private case class NnSettings(activationFunction: ActivationFunction = ActivationFunction.LOGISTIC_SIGMOID,
                                hiddenUnits: Int = 4, learningRate: Double = 5e-4, momentum: Double = 0.0,
                                weightDecay: Double = 0.0)

  private val tuner = new SettingsTuner[NnSettings](seed) {
    override protected def numAxes: Int = 4

    override protected def variationsAlogAxis(setting: NnSettings, axis: Int): Seq[NnSettings] = axis match {
      case 0 =>
        val hiddenUnitsToTry = max(2, setting.hiddenUnits - 5).to(setting.hiddenUnits + 5)
        hiddenUnitsToTry.map(u => setting.copy(hiddenUnits = u))

      case 1 =>
        val learningRatesRange = setting.learningRate * 2 - setting.learningRate / 2
        val steps = 10
        val step = learningRatesRange/(steps - 1)
        val learningRatesToTry = 0.until(steps).map(i => setting.learningRate/2 + step*i)
        learningRatesToTry.map(r => setting.copy(learningRate = r))

      case 2 =>
        val weightDecaysToTry = TuningHelpers.valuesBetweenZeroAndOneAround(setting.weightDecay, 10).map(_ * 0.1)
        weightDecaysToTry.map(d => setting.copy(weightDecay = d))

      case _ =>
        val momentumsToTry = TuningHelpers.valuesBetweenZeroAndOneAround(setting.momentum, 10)
        momentumsToTry.map(m => setting.copy(momentum = m))
    }

    override protected def evalSettings(settings: NnSettings): Double = {
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

  private def exploreSettings(): Seq[(NnSettings, Double)] = {
    tuner.tune(NnSettings(), maxIterations = 25)
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
