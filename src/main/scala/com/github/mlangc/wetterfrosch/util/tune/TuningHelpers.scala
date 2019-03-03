package com.github.mlangc.wetterfrosch.util.tune

import scala.math.exp
import scala.util.Random

class TuningHelpers(rng: Random = Random) {
  def valuesBetweenZeroAndOneAround(value: Double, n: Int): Seq[Double] = {
    require(n > 0)
    require(value >= 0.0 && value <= 1.0)

    if (n == 1) {
      Seq(value)
    } else if (value == 0.0) {
      value +: (-n + 1).to(-1).map(y => exp(y))
    } else if (value == 1.0) {
      valuesBetweenZeroAndOneAround(0, n).map(1.0 - _)
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
    doubleBetweenAandBusingDoublesBetweenZeroAndOne(current, a, b, n)(valuesBetweenZeroAndOneAround)
  }

  def randomDoublesBetweenAandB(current: Double, a: Double, b: Double, n: Int): Seq[Double] = {
    doubleBetweenAandBusingDoublesBetweenZeroAndOne(current, a, b, n)(randomDoublesBetweenZeroAndOne)
  }

  def randomDoublesBetweenZeroAndOne(value: Double, n: Int): Seq[Double] = {
    assert(value <= 1)
    assert(value >= 0)
    val eps = 1e-9

    if (value > 1.0 - eps) {
      randomDoublesBetweenZeroAndOne(eps, n).map(1 - _)
    } else if (value < eps) {
      randomDoublesBetweenZeroAndOne(eps, n)
    } else {
      val s = math.min(value, 1 - value)
      val lambda = 3

      Stream.continually(rng.nextGaussian()/lambda * s + value)
        .map(v => if (v > 1) 1.0 else if (v < 0) 0.0 else v)
        .distinct
        .take(n)
        .toArray
    }
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

  private def doubleBetweenAandBusingDoublesBetweenZeroAndOne
  (current: Double, a: Double, b: Double, n: Int)
  (doublesBetweenZeroAndOne: (Double, Int) => Seq[Double]): Seq[Double] = {
    require(a < b)
    require(current >= a)
    require(current <= b)

    val scale = b - a
    val offset = a
    val mapped = (current - offset)/scale
    doublesBetweenZeroAndOne(mapped, n).map(v => v*scale + offset)
  }


}
