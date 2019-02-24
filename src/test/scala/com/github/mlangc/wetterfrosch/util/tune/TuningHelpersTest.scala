package com.github.mlangc.wetterfrosch.util.tune

import com.github.mlangc.wetterfrosch.util.tune.TuningHelpers.doublesBetweenAandB
import org.scalatest.FreeSpec

class TuningHelpersTest extends FreeSpec {
  "Candidates generation" - {
    "values between" - {
      "-1 and 1" in {
        val n = 10
        val values = doublesBetweenAandB(0, -1, 1, n)
        assert(values.size == n)
        assert(values.distinct.size == n)
        assert(values.head == values.min)
        assert(values.max == values.last)
        assert(values.head < 0.0)
        assert(values.last > 0.0)
      }

      "0 and 10"  - {
        "at 0" in {
          val n = 10
          val values = doublesBetweenAandB(0, 0, 10, n)
          assert(values.size == n)
          assert(values.distinct.size == n)
          assert(values.min == 0.0)
        }

        "at 10" in {
          val n = 10
          val values = doublesBetweenAandB(10, 0, 10, n)
          assert(values.size == n)
          assert(values.distinct.size == n)
          assert(values.max == 10)
        }
      }

      "2 and 15" - {
        "mapping to int" in {
          val n = 8
          val values = doublesBetweenAandB(8, 2, 15, 8)
            .map(_.toInt)
            .distinct

          assert(values.size > 3)
          assert(values.size < 8)
          assert(values.max > 10)
          assert(values.min < 6)
        }
      }
    }
  }

  "avg" in {
    assert(TuningHelpers.avg(Seq(1, 2, 3))(x => x.toDouble).contains(2.0))
    assert(TuningHelpers.avg(100)(1.0) == 1.0)
  }
}
