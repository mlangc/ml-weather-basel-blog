package com.github.mlangc.wetterfrosch.smile

import com.github.mlangc.wetterfrosch.CrossValidator

package object implicits {
  implicit def smileCrossValidator: CrossValidator[AbstractSmileRegressionTrainer] = SmileCrossValidator
}
