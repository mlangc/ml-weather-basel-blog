package com.github.mlangc.wetterfrosch

import at.lnet.wetterfrosch.custom.PersistenceModelSingleValuePredictor
import at.lnet.wetterfrosch.dl4j.SingleValueOutputRnnTrainer
import at.lnet.wetterfrosch.smile.{SmileLassoRegressionSingleValueTrainer, SmileRidgeRegressionTrainer}
import com.cibo.evilplot.colors.HTMLNamedColors
import com.cibo.evilplot.displayPlot
import com.cibo.evilplot.numeric.Point
import com.cibo.evilplot.plot.{Overlay, ScatterPlot}
import com.cibo.evilplot.plot.aesthetics.DefaultTheme._
import com.cibo.evilplot.plot.renderers.PointRenderer
import com.github.mlangc.wetterfrosch.custom.MeanSingleValuePredictorTrainer
import com.github.mlangc.wetterfrosch.custom.PersistenceModelSingleValuePredictor
import com.github.mlangc.wetterfrosch.dl4j.SingleValueOutputRnnTrainer
import com.github.mlangc.wetterfrosch.smile.SmileLassoRegressionSingleValueTrainer
import com.github.mlangc.wetterfrosch.smile.SmileRidgeRegressionTrainer
import com.typesafe.scalalogging.StrictLogging

object Wetterfrosch extends StrictLogging {
  private def seed = 42
  private def timeSeriesLen = 2
  private def batchSize = 64
  private val evaluator = new SingleValueRegressionEvaluator
  private def targetCol: String = HistoryExportCols.TotalPrecipitationDaily

  def main(args: Array[String]): Unit = {
    val exportData = new HistoryExportData()
    val (trainTestData, plotData) = cleanData(exportData.csv20180830).partition { r =>
      val year = r(HistoryExportCols.Year)
      lazy val month = r(HistoryExportCols.Month)
      lazy val day = r(HistoryExportCols.Day)

      if (year < 2018) true
      else if (month < 7) true
      else if (month == 7 && day < 31) true
      else if (month > 8) throw new AssertionError(s"Unexpected month $month")
      else false
    }

    val trainTestSplit = new TrainTestSplit(timeSeriesLen, trainTestData, seed)

    train(new MeanSingleValuePredictorTrainer, trainTestSplit)
    train(new SmileLassoRegressionSingleValueTrainer, trainTestSplit)
    evalAndLog(new PersistenceModelSingleValuePredictor(targetCol), trainTestSplit)
    val rnnModel = trainRnn(trainTestSplit)
    val regModel = trainRidgeRegression(trainTestSplit)

    makeNicePlots(rnnModel, regModel, plotData)
  }

  def makeNicePlots(rnnModel: SingleValuePredictor, regModel: SingleValuePredictor, plotData: Seq[Map[String, Double]]): Unit = {
    val actual = {
      plotData
        .tail
        .map(r => Point(r(HistoryExportCols.Day), r(targetCol)))
    }

    val regPredictions = {
      regModel.predict(plotData.init.map(Seq(_))).zip(plotData.tail)
        .map { case (p, r) => Point(r(HistoryExportCols.Day), p) }
    }

    val rnnPredictions = {
      rnnModel.predict(plotData.init.map(Seq(_))).zip(plotData.tail)
        .map { case (p, r) => Point(r(HistoryExportCols.Day), p) }
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

  private def evalAndLog(predictor: SingleValuePredictor, trainTestSplit: TrainTestSplit): Unit = {
    val testEvaluation = evaluator.eval(predictor, trainTestSplit.testData)
    val trainEvaluation = evaluator.eval(predictor, trainTestSplit.trainingData)
    logger.info(s"MSE for $predictor on train data: ${trainEvaluation.mse}")
    logger.info(s"MSE for $predictor on test data : ${testEvaluation.mse}")
  }

  private def train(trainer: SingleValuePredictorTrainer, trainTestSplit: TrainTestSplit): SingleValuePredictor = {
    val predictor = trainer.train(trainTestSplit.trainingData, targetCol)
    evalAndLog(predictor, trainTestSplit)
    predictor
  }

  private def trainRidgeRegression(trainTestSplit: TrainTestSplit) = {
    train(new SmileRidgeRegressionTrainer, trainTestSplit)
  }

  private def trainRnn(trainTestSplit: TrainTestSplit) = {
    train(new SingleValueOutputRnnTrainer(seed, batchSize), trainTestSplit)
  }

  private def cleanData(data: Seq[Map[String, Double]]) = {
    data.map { row =>
      row - HistoryExportCols.Hour - HistoryExportCols.Minute
    }
  }
}
