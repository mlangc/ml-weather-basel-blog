package com.github.mlangc.wetterfrosch.smile

import com.github.mlangc.wetterfrosch.HistoryExportColSubsets

object ExamineFfNn extends SmileLabModule {
  override lazy val featuresExtractor = new SelectedColsSmileFeaturesExtractor(HistoryExportColSubsets.ColsFromLastDayForTree4)

  def main(args: Array[String]): Unit = {
    val trainer = new SmileFfNnTrainer
    val trainingExamples = 10
    val actualTrainingData = trainTestSplit.trainingData.take(trainingExamples)
    val predictor = trainer.train(actualTrainingData, targetCol)

    val trainEvaluation = evaluator.eval(predictor, actualTrainingData)
    val testEvaluation = evaluator.eval(predictor, trainTestSplit.testData)

    println("Training Evaluation: " + trainEvaluation)
    println("Test Evaluation    : " + testEvaluation)
  }
}
