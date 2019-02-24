package com.github.mlangc.wetterfrosch.util.tune

import scala.math.exp

object TuningHelpers {
  def valuesBetweenZeroAndOneAround(value: Double, n: Int): Seq[Double] = {
    require(n > 0)
    require(value >= 0.0 && value <= 1.0)

    if (n == 1) {
      Seq(value)
    } else if (value == 0.0) {
      value +: (-n + 1).to(-1).map(y => exp(y))
    } else if (value == 1.0) {
      TuningHelpers.valuesBetweenZeroAndOneAround(0, n).map(1.0 - _)
    } else if (value > 0.0 && value < 1.0) {
      val left = value/2
      val right = value + (1 - value)/2
      val step = (right - left)/(n - 1)
      0.until(n).map(i => left + step*i)
    } else {
      throw new IllegalArgumentException("" + value)
    }
  }

  def doublesBetweenAandB(current: Double, a: Double, b: Double, n: Int): Seq[Double] = {
    require(a < b)
    require(current >= a)
    require(current <= b)

    val scale = b - a
    val offset = a
    val mapped = (current - offset)/scale
    valuesBetweenZeroAndOneAround(mapped, n).map(v => v*scale + offset)
  }

  def avg[T](ts: Seq[T])(f: T => Double): Option[Double] = {
    if (ts.isEmpty) None else {
      Some(ts.map(f).sum / ts.size)
    }
  }

  def avg(n: Int)(thunk: => Double): Double = {
    require(n > 0)
    avg(Seq.fill(n)(thunk))(identity[Double]).get
  }

  def formatMetricWithSetting[T](pair: (T, Double)): String = {
    f"${pair._2}%10.2f - ${pair._1}"
  }
}
