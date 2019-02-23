package com.github.mlangc.wetterfrosch

import _root_.smile.math.Math
import at.ipsquare.commons.core.util.PerformanceLogger
import com.cibo.evilplot.colors.HTMLNamedColors
import com.cibo.evilplot.displayPlot
import com.cibo.evilplot.numeric.Point
import com.cibo.evilplot.plot.aesthetics.DefaultTheme._
import com.cibo.evilplot.plot.renderers.PointRenderer
import com.cibo.evilplot.plot.{Overlay, ScatterPlot}
import com.github.mlangc.wetterfrosch.HistoryExportCols.Day
import com.github.mlangc.wetterfrosch.custom.MeanSingleValuePredictorTrainer
import com.github.mlangc.wetterfrosch.dl4j.{Dl4jFfNnSingleValuePredictorTrainer, SingleValueOutputRnnTrainer}
import com.github.mlangc.wetterfrosch.smile._
import com.github.mlangc.wetterfrosch.smile.implicits._
import com.github.mlangc.wetterfrosch.util.UtilityModule
import com.typesafe.scalalogging.StrictLogging

object Wetterfrosch extends ExportDataModule with UtilityModule with StrictLogging {
  private def numFolds = 2
  private def nCvRuns = 1

  override def seed: Int = super.seed

  //override lazy val historyExportRowTransformers: HistoryExportRowTransformers = new HistoryExportRowTransformers(
  //  general = Seq(ExportDataTransformations.addTimeOfYearCols(_, keepOrigCols = true))
  //)

  def main(args: Array[String]): Unit = {
    PerformanceLogger.timedExec { () =>
      val timeSeriesLen: Int = 1
      val useHourlyData = false
      val hourlyDataStepSize = 1
      val allCols = if (useHourlyData) exportData.csvHourly.head.keySet else exportData.csvDaily.head.keySet
      val selectedColsForFeatureExtraction = allCols - HistoryExportCols.Year - HistoryExportCols.Month - HistoryExportCols.Day

      val suffix = {
        if (!useHourlyData) s"$timeSeriesLen"
        else s"${timeSeriesLen}h"
      }

      Math.setSeed(seed)

      val labeledData = {
        if (useHourlyData)
          labeledDataAssembler.assembleHourlyData(timeSeriesLen, hourlyDataStepSize)
        else
          labeledDataAssembler.assemblyDailyData(timeSeriesLen)
      }

      val (trainTestData, plotData) = labeledData.partition(labeledDataFilter)

      val trainTestSplit = new TrainTestSplit(trainTestData, seed)
      //val (rnnModel, rnnEvaluations) = trainRnn(trainTestSplit)
      val (olsModel, olsEvaluations) = trainOls(trainTestSplit)
      val (ffNnModel, ffNnEvaluations) = trainFfNn(trainTestSplit)
      val (meanModel, _) = trainMeanModel(trainTestSplit)

      val smileFeaturesExtractor = new SelectedColsSmileFeaturesExtractor(
        Seq.fill(timeSeriesLen)(selectedColsForFeatureExtraction))

      val evaluations: Array[Evaluations] = Array(
        //train("Persistence", new PersistenceModelSingleValuePredictorDummyTrainer, trainTestSplit)._2,
        //train("Mean", new MeanSingleValuePredictorTrainer, trainTestSplit)._2,
        //train(s"Tree-$suffix", new SmileRegressionTreeTrainer(11), trainTestSplit)._2,
        //train(s"Gbm-$suffix", new SmileGbmRegressionTrainer(500, 6), trainTestSplit)._2,
        //train(s"Dl4jFfNn-$timeSeriesLen", new SmileFfNnTrainer(), trainTestSplit)._2
        //train(s"Ridge-$suffix", new SmileRidgeRegressionTrainer(1), trainTestSplit)._2
        //train(s"Forst-$suffix", new SmileRandomForestRegressionTrainer(nTrees = 500), trainTestSplit)._2,
        //train(s"SmileFfNn-$suffix", new SmileFfNnTrainer(), trainTestSplit)._2
        //regEvaluations
      )

      //println(evaluationsToCsv(evaluations))
      displayPlot(plotUtils.compareVisually(targetCol, plotData, olsModel, ffNnModel, meanModel))
      Unit
    }
  }

  private def evaluationsToCsv(evaluations: Array[Evaluations]): String = {
    val header = "Model,RMSE Test,RMSE Train, MAE Test, MAE Train, RMSE Cv, MAE Cv\n"
    evaluations
      .map(ev => f"${ev.name},${ev.test.rmse}%.1fmm, ${ev.train.rmse}%.1fmm, ${ev.test.mae}%.1fmm, ${ev.train.mae}%.1fmm, ${ev.cv.rmse}%.1fmm, ${ev.cv.mae}%.1fmm")
      .mkString(header, "\n", "\n")
  }

  private def makeNicePlots(rnnModel: SingleValuePredictor, regModel: SingleValuePredictor, plotData: Seq[Map[String, Double]]): Unit = {
    val actual = {
      plotData
        .tail
        .map(r => Point(r(Day), r(targetCol)))
    }

    val regPredictions = {
      regModel.predict(plotData.init.map(Seq(_))).zip(plotData.tail)
        .map { case (p, r) => Point(r(Day), p) }
    }

    val rnnPredictions = {
      rnnModel.predict(plotData.init.map(Seq(_))).zip(plotData.tail)
        .map { case (p, r) => Point(r(Day), p) }
    }

    displayPlot {
      Overlay(
          ScatterPlot(actual, pointRenderer = Some(PointRenderer.default(color = Some(HTMLNamedColors.black)))),
          ScatterPlot(regPredictions, pointRenderer = Some(PointRenderer.default(color = Some(HTMLNamedColors.red)))),
          ScatterPlot(rnnPredictions, pointRenderer = Some(PointRenderer.default(color = Some(HTMLNamedColors.green))))
        ).xAxis()
         .yAxis()
         .frame()
    }
  }

  private case class Evaluations(name: String,
                                 test: SingleValueRegressionEvaluation,
                                 train: SingleValueRegressionEvaluation,
                                 cv: SingleValueRegressionEvaluation)

  private def train[TrainerType <: SingleValuePredictorTrainer](name: String,
                                                                trainer: TrainerType,
                                                                trainTestSplit: TrainTestSplit)
                                                               (implicit crossValidator: CrossValidator[TrainerType])
  : (SingleValuePredictor, Evaluations) = {
    val cvEvaluation = crossValidator.crossValidate(trainer, trainTestSplit.trainingData, targetCol, numFolds, nCvRuns)
    val predictor = trainer.train(trainTestSplit.trainingData, targetCol)
    val testEvaluation = evaluator.eval(predictor, trainTestSplit.testData)
    val trainEvaluation = evaluator.eval(predictor, trainTestSplit.trainingData)
    val evaluations = Evaluations(name, testEvaluation, trainEvaluation, cvEvaluation)
    (predictor, evaluations)
  }

  private def trainRidgeRegression(trainTestSplit: TrainTestSplit) = {
    train("Ridge Regression", new SmileRidgeRegressionTrainer, trainTestSplit)
  }

  private def trainOls(trainTestSplit: TrainTestSplit) = {
    train("OLS", new SmileOlsTrainer(), trainTestSplit)
  }

  private def trainRnn(trainTestSplit: TrainTestSplit) = {
    train("RNN", new SingleValueOutputRnnTrainer(seed, 16), trainTestSplit)
  }

  private def  trainFfNn(trainTestSplit: TrainTestSplit) = {
    train("FFNN", new SmileFfNnTrainer(), trainTestSplit)
  }

  private def  trainMeanModel(trainTestSplit: TrainTestSplit) = {
    train("Mean", new MeanSingleValuePredictorTrainer(), trainTestSplit)
  }
}
