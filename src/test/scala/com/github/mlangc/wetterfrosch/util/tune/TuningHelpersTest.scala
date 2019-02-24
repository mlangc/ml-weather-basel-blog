package com.github.mlangc.wetterfrosch.util.tune

import com.github.mlangc.wetterfrosch.util.tune.TuningHelpers.valuesBetweenAandB
import org.scalatest.FreeSpec

class TuningHelpersTest extends FreeSpec {
  "Candidates generation" - {
    "values between" - {
      "-1 and 1" in {
        val n = 10
        val values = valuesBetweenAandB(0, -1, 1, n)
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
          val values = valuesBetweenAandB(0, 0, 10, n)
          assert(values.size == n)
          assert(values.distinct.size == n)
          assert(values.min == 0.0)
        }

        "at 10" in {
          val n = 10
          val values = valuesBetweenAandB(10, 0, 10, n)
          assert(values.size == n)
          assert(values.distinct.size == n)
          assert(values.max == 10)
        }
      }
    }
  }
}
