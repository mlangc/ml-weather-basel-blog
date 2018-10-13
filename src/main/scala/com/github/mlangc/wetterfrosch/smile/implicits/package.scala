package com.github.mlangc.wetterfrosch.smile

import com.github.mlangc.wetterfrosch.CrossValidator
import com.github.mlangc.wetterfrosch.util.store.{Pickler, SerializablePickler}
import smile.regression.Regression

package object implicits {
  implicit def smileCrossValidator: CrossValidator[AbstractSmileRegressionTrainer] = SmileCrossValidator
  implicit def smileRegressionPickler: Pickler[Regression[Array[Double]] with java.io.Serializable] = {
    new SerializablePickler[Regression[Array[Double]] with java.io.Serializable]
  }
}
