package com.github.mlangc.wetterfrosch

import com.github.mlangc.wetterfrosch.ExportDataTransformations.addTimeOfYearCols
import com.github.mlangc.wetterfrosch.HistoryExportCols._
import org.scalatest.FreeSpec

class ExportDataTransformationsTest extends FreeSpec {
  "add time of year columns" - {
    "with a few simple examples" in {
      locally {
        val (t1, t2) = toYearDates(2018, 1, 1)
        assert(t1 < 0)
        assert(t2 < 0)
      }

      locally {
        val (t1, t2) = toYearDates(2018, 3, 25)
        assert(t1 > 0)
        assert(t2 < 0)
      }

      locally {
        val (t1, t2) = toYearDates(2004, 7, 12)
        assert(t1 > 0)
        assert(t2 > 0)
        assert(t2 > t1)
      }

      locally {
        val (t1, t2) = toYearDates(2000, 9, 30)
        assert(t1 < 0)
        assert(t2 > 0)
      }

      locally {
        val (t1, t2) = toYearDates(1981, 5, 1)
        val (u1, u2) = toYearDates(1981, 6, 10)

        assert(t1 > t2)
        assert(u1 > u2)
        assert(t1 < u1)
        assert(t2 < u2)
      }
    }

    "make sure that original cols" - {
      "are left/removed as requested" in {
        val row = mkRowForDate(2018, 10, 6)
        val transformedWiOrigCols = addTimeOfYearCols(row, keepOrigCols = true)
        val transformedWoOrigCols = addTimeOfYearCols(row, keepOrigCols = false)

        assert(timeOfYearCols(transformedWiOrigCols) == timeOfYearCols(transformedWoOrigCols))
        assert(transformedWoOrigCols.size == 2)
        assert(transformedWiOrigCols.size == 5)
      }
    }
  }

  private def mkRowForDate(year: Int, month: Int, day: Int): Map[String, Double] = {
    Map(Year -> year, Month -> month, Day -> day)
  }

  private def toYearDates(year: Int, month: Int, day: Int): (Double, Double) = {
    val row = mkRowForDate(year, month, day)
    val transformedRow = addTimeOfYearCols(row)
    timeOfYearCols(transformedRow)
  }

  private def timeOfYearCols(row: Map[String, Double]): (Double, Double) = {
    val cols @ (t1, t2) = (row(TimeOfYear1), row(TimeOfYear2))

    assert(t1 >= -1 && t1 <= 1)
    assert(t2 >= -1 && t2 <= 1)

    cols
  }
}

