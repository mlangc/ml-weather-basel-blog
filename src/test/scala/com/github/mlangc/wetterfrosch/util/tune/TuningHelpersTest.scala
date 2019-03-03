package com.github.mlangc.wetterfrosch.util.tune

import org.scalatest.FreeSpec

class TuningHelpersTest extends FreeSpec {
  private val tuningHelpers = new TuningHelpers()
  import tuningHelpers.doublesBetweenAandB
  import tuningHelpers.avg

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

      "random values beteen" - {
        "-1 and 1 centered around 0" in {
          val n = 100*1000
          val (current, a, b) = (0, -1, 1)
          val values = tuningHelpers.randomDoublesBetweenAandB(current, a, b, n)
          assert(values.size == n)
          assert(values.filter(v => v < a || v > b).isEmpty)

          val (h1, h2) = values.partition(_ < 0)
          assert(math.abs(h1.size - h2.size) < 20000)
          assert(values.filter(v => v == a || v == b).nonEmpty)
        }

        "between 0 and 1 centered around 0.1" in {
          val n = 100
          val (current, a, b) = (0.1, 0, 1)
          val values = tuningHelpers.randomDoublesBetweenAandB(current, a, b, n)
          assert(values.distinct.size == n)
          val (h1, h2) = values.partition(_ < 0.2)
          assert(h1.size > h2.size)
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
    assert(avg(Seq(1, 2, 3))(x => x.toDouble).contains(2.0))
    assert(avg(100)(1.0) == 1.0)
  }
}
