package com.github.mlangc.wetterfrosch

import java.time.LocalDate

import _root_.smile.math.Math
import at.ipsquare.commons.core.util.PerformanceLogger
import com.cibo.evilplot.colors.HTMLNamedColors
import com.cibo.evilplot.displayPlot
import com.cibo.evilplot.numeric.Point
import com.cibo.evilplot.plot.aesthetics.DefaultTheme._
import com.cibo.evilplot.plot.renderers.PointRenderer
import com.cibo.evilplot.plot.{Overlay, ScatterPlot}
import com.github.mlangc.wetterfrosch.HistoryExportCols.Day
import com.github.mlangc.wetterfrosch.custom.{MeanSingleValuePredictorTrainer, PersistenceModelSingleValuePredictorDummyTrainer, PersistenceModelSingleValuePredictor}
import com.github.mlangc.wetterfrosch.dl4j.SingleValueOutputRnnTrainer
import com.github.mlangc.wetterfrosch.smile._
import com.github.mlangc.wetterfrosch.smile.implicits._
import com.typesafe.scalalogging.StrictLogging

object Wetterfrosch extends ExportDataModule with StrictLogging {
  private val numFolds = 25
  private val nCvRuns = 50

  def main(args: Array[String]): Unit = {
    PerformanceLogger.timedExec { () =>
      val timeSeriesLen: Int = 1
      val useHourlyData = false
      val hourlyDataStepSize = 1

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

      val (trainTestData, plotData) = labeledData.partition { rs =>
        val date = ExportDataUtils.localDateFrom(rs.last)
        date.isBefore(LocalDate.of(2018, 7, 31))
      }

      val trainTestSplit = new TrainTestSplit(trainTestData, seed)
      //val (rnnModel, rnnEvaluations) = trainRnn(trainTestSplit)
      //val (regModel, regEvaluations) = trainRidgeRegression(trainTestSplit)

      val smileFeaturesExtractor = new SelectedColsSmileFeaturesExtractor(HistoryExportColSubsets.ColsFromLastDayForTree4)

      val evaluations: Array[Evaluations] = Array(
        //train("Persistence", new PersistenceModelSingleValuePredictorDummyTrainer, trainTestSplit)._2,
        //train("Mean", new MeanSingleValuePredictorTrainer, trainTestSplit)._2,
        train(s"Tree-$suffix", new SmileRegressionTreeTrainer(4, DefaultSmileFeaturesExtractor), trainTestSplit)._2,
        //train(s"Gbm-$suffix", new SmileGbmRegressionTrainer(100, 4), trainTestSplit)._2,
        train(s"OLS-$timeSeriesLen", new SmileOlsTrainer(smileFeaturesExtractor), trainTestSplit)._2,
        //train(s"Ridge-$suffix", new SmileRidgeRegressionTrainer(1), trainTestSplit)._2
        //regEvaluations
      )

      println(evaluationsToCsv(evaluations))
      //makeNicePlots(rnnModel, regModel, plotData)
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

  private def trainRnn(trainTestSplit: TrainTestSplit) = {
    train("RNN", new SingleValueOutputRnnTrainer(seed, batchSize), trainTestSplit)
  }
}
