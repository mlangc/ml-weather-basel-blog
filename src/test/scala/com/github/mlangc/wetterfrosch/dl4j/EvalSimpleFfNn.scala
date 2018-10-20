package com.github.mlangc.wetterfrosch.dl4j

import com.github.mlangc.wetterfrosch.smile.SmileLabModule

object EvalSimpleFfNn extends SmileLabModule {
  def main(args: Array[String]): Unit = {
    val batchSize = 1024
    val epochs = 100
    val trainer = new Dl4jFfNnSingleValuePredictorTrainer(seed, batchSize, epochs)
    val trainingData = trainTestSplit.trainingData.take(128)

    val model = trainer.train(trainingData, targetCol)
    val evaluation = evaluator.eval(model, trainingData)

    println(s"Evaluation: $evaluation")
  }
}
