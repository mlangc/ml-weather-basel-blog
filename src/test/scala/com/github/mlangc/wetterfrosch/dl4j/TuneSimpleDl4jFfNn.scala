package com.github.mlangc.wetterfrosch.dl4j

import com.github.mlangc.wetterfrosch.HistoryExportColSubsets
import com.github.mlangc.wetterfrosch.util.tune.SettingsTuner
import org.deeplearning4j.nn.api.OptimizationAlgorithm
import org.deeplearning4j.nn.conf.layers.{DenseLayer, OutputLayer}
import org.deeplearning4j.nn.conf.{MultiLayerConfiguration, NeuralNetConfiguration, Updater}
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork
import org.deeplearning4j.nn.weights.WeightInit
import org.nd4j.linalg.activations.Activation
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator
import org.nd4j.linalg.learning.config._
import org.nd4j.linalg.lossfunctions.LossFunctions

object TuneSimpleDl4jFfNn extends Dl4jLabModule {
  def main(args: Array[String]): Unit = {
    val millis = System.currentTimeMillis()
    println(s"Using ${cfg.epochs} epochs for training with ${cfg.nEvals} evaluations for each setting")
    println(s"Maximal number of iterations is ${cfg.maxIterations} with no more than ${cfg.maxRetries} retries")
    println()
    println("Exploring different settings...")

    val explorations = tuner.tune(NnSettings(), maxIterations = cfg.maxIterations, maxRetries = cfg.maxRetries)

    println()
    println("Exploration done")
    println("Summary:")
    explorations.foreach { p =>
      println(tuningHelpers.formatMetricWithSetting(p))
    }
    val elapsed = System.currentTimeMillis() - millis
    println("")
    println(s"Done after ${elapsed}ms")
  }


  private object cfg {
    def epochs = 100
    def nEvals = 5
    def maxIterations = 100
    def maxRetries = 3
    def selectedCols: Set[String] = HistoryExportColSubsets.ColsFromLastDayForTree23
  }

  override lazy val featuresExtractor = new SelectedColsDl4jFfNnFeaturesExtractor(cfg.selectedCols)

  private abstract class UpdaterSpec {
    def updater: Updater
    def instantiate(): IUpdater
    def learningRate: Double
    def withLearningRate(r: Double): UpdaterSpec

    override def toString: String = {
      f"UpdaterSpec($updater, $learningRate%.2e)"
    }
  }

  private object UpdaterSpec {
    case class UpdateSpecImpl(updater: Updater, learningRate: Double, ctor: Double => IUpdater) extends UpdaterSpec {
      override def instantiate(): IUpdater = ctor(learningRate)
      override def withLearningRate(r: Double): UpdaterSpec = copy(learningRate = r)
    }

    def fromCtorWithDefaultArgs(updater: Updater, defaultLearningRate: Double, ctor: Double => IUpdater): UpdaterSpec = {
      UpdateSpecImpl(updater, defaultLearningRate, ctor)
    }
  }

  private case class RmsPropUpdaterSpec(learningRate: Double = 5.20e-01,
                                decay: Double = 9.60e-01,
                                epsilon: Double = RmsProp.DEFAULT_RMSPROP_EPSILON) extends UpdaterSpec {
    def updater: Updater = Updater.RMSPROP
    def instantiate(): IUpdater = new RmsProp(learningRate, decay, epsilon)
    def withLearningRate(r: Double): UpdaterSpec = copy(learningRate = r)

    override def toString: String = {
      f"UpdaterSpec($updater, $learningRate%.2e, $decay%.2e, $epsilon%.2e)"
    }
  }

  private val DefaultAdamSpec = UpdaterSpec.fromCtorWithDefaultArgs(
    Updater.ADAM, 3.00e00,
    new Adam(_, Adam.DEFAULT_ADAM_BETA1_MEAN_DECAY, Adam.DEFAULT_ADAM_BETA2_VAR_DECAY, Adam.DEFAULT_ADAM_EPSILON))

  private val DefaultSgdSpec = UpdaterSpec.fromCtorWithDefaultArgs(
    Updater.SGD, 5.33e-3, new Sgd(_)
  )

  private val DefaultNesterovsSpec = UpdaterSpec.fromCtorWithDefaultArgs(
    Updater.NESTEROVS, 3.00e-2, new Nesterovs(_, Nesterovs.DEFAULT_NESTEROV_MOMENTUM)
  )

  private val DefaultRmsPropSpec = RmsPropUpdaterSpec()

  //private val DefaultUpdaterSpecs = Array(DefaultAdamSpec, DefaultSgdSpec, DefaultNesterovsSpec, DefaultRmsPropSpec)
  private val ActiveDefaultUpdaterSpecs = Array(DefaultRmsPropSpec)

  private case class NnSettings(batchSize: Int = 128,
                                hiddenLayers: Int = 8,
                                activation: Activation = Activation.SIGMOID,
                                updater: UpdaterSpec = DefaultRmsPropSpec,
                                reshuffle: Boolean = true)

  private def numInputs: Int = cfg.selectedCols.size

  private val tuner = new SettingsTuner[NnSettings](rng) {
    override protected def numAxes: Int = ActiveDefaultUpdaterSpecs.size

    override protected def variationsAlongAxis(setting: NnSettings, history: Map[NnSettings, Double], axis: Int): Seq[NnSettings] = {
      if (axis < ActiveDefaultUpdaterSpecs.size) {
        val defaultSpec = ActiveDefaultUpdaterSpecs(axis)
        val bestUpdaterSoFar: UpdaterSpec = history
          .filterKeys(_.updater.updater == defaultSpec.updater)
          .toSeq
          .reduceOption((l, r) => if (l._2 < r._2) l else r)
          .map(_._1.updater).getOrElse(defaultSpec)

        bestUpdaterSoFar match {
          case bestUpdaterSoFar: RmsPropUpdaterSpec =>
            val minLearningRate = bestUpdaterSoFar.learningRate/2
            val maxLearningRate = bestUpdaterSoFar.learningRate*2
            val learningRates = tuningHelpers.randomDoublesBetweenAandB(bestUpdaterSoFar.learningRate, minLearningRate, maxLearningRate,  25)
            val decays = tuningHelpers.randomDoublesBetweenZeroAndOne(bestUpdaterSoFar.decay, 25)

            learningRates.zip(decays).map { case (learningRate, decay) =>
              val updater = bestUpdaterSoFar.copy(learningRate = learningRate, decay = decay)
              setting.copy(updater = updater)
            }

          case _ =>
            val learningRate = bestUpdaterSoFar.learningRate
            val a = learningRate/2
            val b = learningRate*2
            val l = b - a
            val n = 16
            val s = l/(n-1)
            val learningRates: Seq[Double] = 0.until(n).map(i => a + i*s).filterNot(_ == learningRate)
            learningRates
              .map(learningRate => setting.copy(updater = bestUpdaterSoFar.withLearningRate(learningRate)))
              .filterNot(history.keySet.contains(_))
        }
      } else {
        (axis - ActiveDefaultUpdaterSpecs.size) match {
          case 0 =>
            val newSetting = setting.copy(reshuffle = !setting.reshuffle)
            if (history.keySet.contains(newSetting)) Seq()
            else Seq(newSetting)

//          case 0|1|2 =>
//            val batchSizes = TuningHelpers.doublesBetweenAandB(setting.batchSize, 32, 1024*4, 15)
//                .map(_.toInt)
//                .filterNot(_ == setting.batchSize)
//                .distinct
//
//            batchSizes.map(batchSize => setting.copy(batchSize = batchSize))

//          case 1|2 =>
//            TuningHelpers.doublesBetweenAandB(setting.hiddenLayers, 2, 15, 8)
//              .map(_.toInt)
//              .filterNot(_ == setting.hiddenLayers)
//              .distinct
//              .map(hiddenLayers => setting.copy(hiddenLayers = hiddenLayers))

//          case 2 =>
//            Seq(Activation.SIGMOID, Activation.TANH)
//              .filterNot(_ == setting.activation)
//              .map(activation => setting.copy(activation = activation))
        }
      }
    }

    override protected def evalSettings(settings: NnSettings): Double = {
      val trainingIter = getTrainingIter(settings.batchSize)

      tuningHelpers.avg(cfg.nEvals) {
        val nn = initNn(settings)
        0.until(cfg.epochs).foreach { _ =>
          nn.fit(trainingIter)

          if (settings.reshuffle) trainingIter.reshuffle(rng)
          else trainingIter.reset()
        }

        if (nn.getLayer(0).params().getDouble(0).isNaN) {
          1e10
        } else {
          val evaluation = nn.evaluateRegression(trainingIter)
          evaluation.averagerootMeanSquaredError()
        }
      }
    }

    override protected def onProgress(iteration: Int, bestSettings: NnSettings, bestRmse: Double): Unit = {
      println(f"  Best setting after iteration $iteration%4d: " + tuningHelpers.formatMetricWithSetting(bestSettings -> bestRmse))
    }
  }

  private def toMultilayerConfig(settings: NnSettings): MultiLayerConfiguration = {
    val nnConf: MultiLayerConfiguration = new NeuralNetConfiguration.Builder()
      .seed(seed)
      .weightInit(WeightInit.XAVIER)
      .optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
      .updater(settings.updater.instantiate())
      .learningRate(settings.updater.learningRate)
      .list()
      .layer(0, new DenseLayer.Builder()
        .nIn(numInputs).nOut(settings.hiddenLayers)
        .activation(settings.activation).build())
      .layer(1, new OutputLayer.Builder(LossFunctions.LossFunction.MSE)
        .nIn(settings.hiddenLayers).nOut(1)
        .activation(Activation.IDENTITY).build())
      .pretrain(false)
      .backprop(true)
      .build()

    nnConf
  }

  private def initNn(settings: NnSettings): MultiLayerNetwork = {
    val nn = new MultiLayerNetwork(toMultilayerConfig(settings))
    nn.init()
    nn
  }

  private def getTrainingIter(batchSize: Int): DataSetIterator with HasShuffleSupport = {
    toLabeledDataSetIter(rng.shuffle(trainTestSplit.trainingData), batchSize)
  }

  private def toLabeledDataSetIter(labeledData: Seq[Seq[Map[String, Double]]], batchSize: Int): DataSetIterator with HasShuffleSupport = {
    featuresExtractor.toFeaturesWithLabels(labeledData, targetCol, batchSize)
  }
}
