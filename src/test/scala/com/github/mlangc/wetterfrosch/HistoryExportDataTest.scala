package com.github.mlangc.wetterfrosch

import com.github.mlangc.wetterfrosch.HistoryExportCols.Day
import com.github.mlangc.wetterfrosch.HistoryExportCols.Hour
import com.github.mlangc.wetterfrosch.HistoryExportCols.Month
import com.github.mlangc.wetterfrosch.HistoryExportCols.SnowfallAmountDaily
import com.github.mlangc.wetterfrosch.HistoryExportCols.SunshineDuration
import com.github.mlangc.wetterfrosch.HistoryExportCols.TempDailyMean
import com.github.mlangc.wetterfrosch.HistoryExportCols.TotalPrecipitationDailySum
import com.github.mlangc.wetterfrosch.HistoryExportCols.WindGustDailyMin
import com.github.mlangc.wetterfrosch.HistoryExportCols.Year
import org.scalatest.FreeSpec
import org.scalatest.OptionValues

class HistoryExportDataTest extends FreeSpec with OptionValues {
  private val data = new HistoryExportData()

  "a few sanity checks" - {
    "on daily data" in {
      val csv = data.csvDaily
      assert(csv.size > 12000)
      assert(csv.size < 15000)

      val firstRow = csv.head
      assert(firstRow.get(Year).value == 1985.0)
      assert(firstRow.get(Day).value == 1.0)
      assert(firstRow.get(TempDailyMean).value == 0.31)
      assert(firstRow.get(WindGustDailyMin).value == 12.6)

      val aug302018 = csv
        .find(r => r(Year) == 2018 && r(Month) == 8 && r(Day) == 30)
        .value
      assert(aug302018(WindGustDailyMin) == 9.36)

      val snowButNoPrecipitation = csv.filter { row =>
        val snow = row(SnowfallAmountDaily)
        val precipitation = row(TotalPrecipitationDailySum)
        snow > 0.85 && precipitation <= 0
      }.map { row =>
        (row(Year),
          row(Month),
          row(Day),
          row(TotalPrecipitationDailySum),
          row(SnowfallAmountDaily))
      }

      assert(snowButNoPrecipitation.isEmpty)
    }

    "on hourly data" in {
      val csv = data.csvHourly

      assert(csv.size > 295*1000)

      val aug20180830t8h = csv.find { r =>
        val year = r(Year)
        val month = r(Month)
        val day = r(Day)
        val hour = r(Hour)

        (year, month, day, hour) match {
          case (2018, 8, 30, 8) => true
          case _ => false
        }
      }.value

      assert(aug20180830t8h(SunshineDuration) == 57.45)
    }
  }
}
