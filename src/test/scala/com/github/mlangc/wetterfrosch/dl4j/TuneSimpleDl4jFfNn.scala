package com.github.mlangc.wetterfrosch.dl4j

import com.github.mlangc.wetterfrosch.{HistoryExportColSubsets, HistoryExportCols}
import com.github.mlangc.wetterfrosch.util.tune.{SettingsTuner, TuningHelpers}
import org.deeplearning4j.nn.api.OptimizationAlgorithm
import org.deeplearning4j.nn.conf.layers.{DenseLayer, OutputLayer}
import org.deeplearning4j.nn.conf.{MultiLayerConfiguration, NeuralNetConfiguration, Updater}
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork
import org.deeplearning4j.nn.weights.WeightInit
import org.nd4j.linalg.activations.Activation
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator
import org.nd4j.linalg.learning.config.{Adam, IUpdater, Nesterovs, RmsProp, Sgd}
import org.nd4j.linalg.lossfunctions.LossFunctions

import scala.util.Random

object TuneSimpleDl4jFfNn extends Dl4jLabModule {
  def main(args: Array[String]): Unit = {
    println(s"Using ${cfg.epochs} epochs for training with ${cfg.nEvals} evaluations for each setting...")
    println()
    println("Exploring different settings...")

    val explorations = tuner.tune(NnSettings(), maxIterations = 250, maxRetries = 50)
    explorations.foreach { p =>
      println(TuningHelpers.formatMetricWithSetting(p))
    }
  }


  private object cfg {
    def epochs = 15
    def nEvals = 25
    def selectedCols = HistoryExportColSubsets.ColsFromLastDayForTree23
  }

  override lazy val featuresExtractor = new SelectedColsDl4jFfNnFeaturesExtractor(cfg.selectedCols)

  private val rng = new Random(seed)

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

  private val DefaultAdamSpec = UpdaterSpec.fromCtorWithDefaultArgs(
    Updater.ADAM, 1.18e2,
    new Adam(_, Adam.DEFAULT_ADAM_BETA1_MEAN_DECAY, Adam.DEFAULT_ADAM_BETA2_VAR_DECAY, Adam.DEFAULT_ADAM_EPSILON))

  private val DefaultSgdSpec = UpdaterSpec.fromCtorWithDefaultArgs(
    Updater.SGD, 3.2e-2, new Sgd(_)
  )

  private val DefaultNesterovsSpec = UpdaterSpec.fromCtorWithDefaultArgs(
    Updater.NESTEROVS, 2.5e-2, new Nesterovs(_, Nesterovs.DEFAULT_NESTEROV_MOMENTUM)
  )

  private val DefaultRmsPropSpec = UpdaterSpec.fromCtorWithDefaultArgs(
    Updater.RMSPROP, 3.33e1,
    new RmsProp(_, RmsProp.DEFAULT_RMSPROP_RMSDECAY, RmsProp.DEFAULT_RMSPROP_EPSILON)
  )

  private val DefaultUpdaterSpecs = Array(DefaultAdamSpec, DefaultSgdSpec, DefaultNesterovsSpec, DefaultRmsPropSpec)

  private case class NnSettings(batchSize: Int = 1350,
                                hiddenLayers: Int = 8,
                                activation: Activation = Activation.SIGMOID,
                                updater: UpdaterSpec = DefaultRmsPropSpec)

  private def numInputs: Int = cfg.selectedCols.size

  private val tuner = new SettingsTuner[NnSettings](seed) {
    override protected def numAxes: Int = DefaultUpdaterSpecs.size + 2

    override protected def variationsAlongAxis(setting: NnSettings, history: Map[NnSettings, Double], axis: Int): Seq[NnSettings] = {
      if (axis < DefaultUpdaterSpecs.size) {
        val defaultSpec = DefaultUpdaterSpecs(axis)
        val bestUpdaterSoFar: UpdaterSpec = history
          .filterKeys(_.updater.updater == defaultSpec.updater)
          .toSeq
          .reduceOption((l, r) => if (l._2 < r._2) l else r)
          .map(_._1.updater).getOrElse(defaultSpec)

        val learningRate = bestUpdaterSoFar.learningRate
        val a = learningRate/2
        val b = learningRate*2
        val l = b - a
        val n = 10
        val s = l/(n-1)
        val learningRates: Seq[Double] = 0.until(n).map(i => a + i*s).filterNot(_ == learningRate)
        learningRates.map(learningRate => setting.copy(updater = bestUpdaterSoFar.withLearningRate(learningRate)))
      } else {
        (axis - DefaultUpdaterSpecs.size) match {
          case 0 =>
            val batchSizes = TuningHelpers.doublesBetweenAandB(setting.batchSize, 32, 1024*4, 15)
                .map(_.toInt)
                .filterNot(_ == setting.batchSize)
                .distinct

            batchSizes.map(batchSize => setting.copy(batchSize = batchSize))

          case 1|2 =>
            TuningHelpers.doublesBetweenAandB(setting.hiddenLayers, 2, 15, 8)
              .map(_.toInt)
              .filterNot(_ == setting.hiddenLayers)
              .distinct
              .map(hiddenLayers => setting.copy(hiddenLayers = hiddenLayers))

//          case 2 =>
//            Seq(Activation.SIGMOID, Activation.TANH)
//              .filterNot(_ == setting.activation)
//              .map(activation => setting.copy(activation = activation))
        }
      }
    }

    override protected def evalSettings(settings: NnSettings): Double = {
      val trainingIter = getTrainingIter(settings.batchSize)

      TuningHelpers.avg(cfg.nEvals) {
        val nn = initNn(settings)
        0.until(cfg.epochs).foreach { _ =>
          nn.fit(trainingIter)
          trainingIter.reset()
        }

        if (nn.getLayer(0).params().getDouble(0).isNaN) {
          1e10
        } else {
          val evaluation = nn.evaluateRegression(trainingIter)
          evaluation.averagerootMeanSquaredError()
        }
      }
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

  private def getTrainingIter(batchSize: Int): DataSetIterator = {
    toLabeledDataSetIter(rng.shuffle(trainTestSplit.trainingData), batchSize)
  }

  private def toLabeledDataSetIter(labeledData: Seq[Seq[Map[String, Double]]], batchSize: Int): DataSetIterator = {
    featuresExtractor.toFeaturesWithLabels(labeledData, targetCol, batchSize)
  }
}
