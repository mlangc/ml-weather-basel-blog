package com.github.mlangc.wetterfrosch

import com.cibo.evilplot.colors.HTMLNamedColors
import com.cibo.evilplot.displayPlot
import com.cibo.evilplot.numeric.Point
import com.cibo.evilplot.plot.{Overlay, ScatterPlot}
import com.cibo.evilplot.plot.aesthetics.DefaultTheme._
import com.cibo.evilplot.plot.renderers.PointRenderer
import com.github.mlangc.wetterfrosch.custom.MeanSingleValuePredictorTrainer
import com.github.mlangc.wetterfrosch.custom.PersistenceModelSingleValuePredictor
import com.github.mlangc.wetterfrosch.dl4j.SingleValueOutputRnnTrainer
import com.github.mlangc.wetterfrosch.smile._
import com.typesafe.scalalogging.StrictLogging

object Wetterfrosch extends StrictLogging {
  private def seed = 42
  private def timeSeriesLen = 2
  private def batchSize = 64
  private val evaluator = new SingleValueRegressionEvaluator
  private def targetCol: String = HistoryExportCols.TotalPrecipitationDailySum

  def main(args: Array[String]): Unit = {
    val exportData = new HistoryExportData()
    val (trainTestData, plotData) = cleanData(exportData.csvDaily).partition { r =>
      val year = r(HistoryExportCols.Year)
      lazy val month = r(HistoryExportCols.Month)
      lazy val day = r(HistoryExportCols.Day)

      if (year < 2018) true
      else if (month < 7) true
      else if (month == 7 && day < 31) true
      else if (month > 8) throw new AssertionError(s"Unexpected month $month")
      else false
    }

    val trainTestSplit = new TrainTestSplit(trainTestData, timeSeriesLen, seed)
    val (rnnModel, rnnEvaluations) = trainRnn(trainTestSplit)
    val (regModel, regEvaluations) = trainRidgeRegression(trainTestSplit)

    val evaluations: Array[Evaluations] = Array(
      train("Mean", new MeanSingleValuePredictorTrainer, trainTestSplit)._2,
      eval("Persistence", new PersistenceModelSingleValuePredictor(targetCol), trainTestSplit),
      train(s"Tree-$timeSeriesLen", new SmileRegressionTreeTrainer(3000), trainTestSplit)._2,
      train(s"Forest-$timeSeriesLen", new SmileGbmRegressionTrainer(500, 30), trainTestSplit)._2,
      train(s"OLS-$timeSeriesLen", new SmileOlsTrainer, trainTestSplit)._2,
      rnnEvaluations, regEvaluations
    )

    println(evaluationsToCsv(evaluations))

    makeNicePlots(rnnModel, regModel, plotData)
  }

  private def evaluationsToCsv(evaluations: Array[Evaluations]): String = {
    val header = "Model,RMSE Test,RMSE Train, MAE Test, MAE Train\n"
    evaluations
      .map(ev => f"${ev.name},${ev.test.rmse}%.1fmm, ${ev.train.rmse}%.1fmm, ${ev.test.mae}%.1fmm, ${ev.train.mae}%.1fmm")
      .mkString(header, "\n", "\n")
  }

  private def makeNicePlots(rnnModel: SingleValuePredictor, regModel: SingleValuePredictor, plotData: Seq[Map[String, Double]]): Unit = {
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

  private case class Evaluations(name: String, test: SingleValueRegressionEvaluation, train: SingleValueRegressionEvaluation)

  private def eval(name: String, predictor: SingleValuePredictor, trainTestSplit: TrainTestSplit): Evaluations = {
    val testEvaluation = evaluator.eval(predictor, trainTestSplit.testData)
    val trainEvaluation = evaluator.eval(predictor, trainTestSplit.trainingData)
    Evaluations(name, testEvaluation, trainEvaluation)
  }

  private def train(name: String, trainer: SingleValuePredictorTrainer, trainTestSplit: TrainTestSplit): (SingleValuePredictor, Evaluations) = {
    val predictor = trainer.train(trainTestSplit.trainingData, targetCol)
    (predictor, eval(name, predictor, trainTestSplit))
  }

  private def trainRidgeRegression(trainTestSplit: TrainTestSplit) = {
    train("Ridge Regression", new SmileRidgeRegressionTrainer, trainTestSplit)
  }

  private def trainRnn(trainTestSplit: TrainTestSplit) = {
    train("RNN", new SingleValueOutputRnnTrainer(seed, batchSize), trainTestSplit)
  }

  private def cleanData(data: Seq[Map[String, Double]]) = {
    data.map { row =>
      row - HistoryExportCols.Hour - HistoryExportCols.Minute
    }
  }
}
