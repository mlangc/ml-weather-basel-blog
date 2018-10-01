package com.github.mlangc.wetterfrosch.smile.examples

import com.github.mlangc.wetterfrosch.smile.SmileLabModule
import org.scalatest.FreeSpec
import smile.validation.RMSE

class CrossValidateTree extends FreeSpec with SmileLabModule {

def crossValidationRmse(trainFeatures: Array[Array[Double]],
                        trainLabels: Array[Double],
                        maxNodes: Int,
                        numFolds: Int): Double = {
  import smile.regression
  import smile.validation

  val Array(rmse) = validation.cv(
    trainFeatures, trainLabels, numFolds, new RMSE) { (features, labels) =>
    regression.cart(features, labels, maxNodes)
  }

  rmse
}

  "Make sure our example works as expected" in {
    val rmse = crossValidationRmse(trainFeatures, trainLabels, 5, 15)
    assert(rmse > 4.3)
    assert(rmse < 4.8)
  }
}
