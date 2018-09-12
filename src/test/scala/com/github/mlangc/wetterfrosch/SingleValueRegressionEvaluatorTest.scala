package com.github.mlangc.wetterfrosch

import org.scalatest.FreeSpec

class SingleValueRegressionEvaluatorTest extends FreeSpec {
  private def mockTargetCol = "mockTarget"
  private val evaluator = new SingleValueRegressionEvaluator

  private class MockPredictor(predictions: Map[Seq[Map[String, Double]], Double]) extends SingleValuePredictor {
    def predict(seqs: Seq[Seq[Map[String, Double]]])(implicit dummy: DummyImplicit): Seq[Double] = {
      seqs.map(predictions)
    }

    def targetCol: String = mockTargetCol
  }

  "eval a single correct prediction" in {
    val seq = Seq(Map.empty[String, Double], Map(mockTargetCol -> 1.0))
    val predictor = new MockPredictor(Map(seq.init -> 1.0))
    val evaluation = evaluator.eval(predictor, Seq(seq))
    assert(evaluation.mse == 0.0)
  }

  "eval a single wrong prediction" in {
    val seq = Seq(Map.empty[String, Double], Map(mockTargetCol -> 1.0))
    val predictor = new MockPredictor(Map(seq.init -> -1.0))
    val evaluation = evaluator.eval(predictor, Seq(seq))
    assert(evaluation.mse == 4.0)
  }

  "eval two predictions" in {
    val seq1 = Seq(Map("a" -> 1.0), Map(mockTargetCol -> 0.0))
    val seq2 = Seq(Map("b" -> 1.0), Map(mockTargetCol -> 0.0))
    val predictor = new MockPredictor(Map(seq1.init -> 3, seq2.init -> 4))
    val evaluation = evaluator.eval(predictor, Seq(seq1, seq2))
    assert(evaluation.mse == 12.5)
  }
}
