package com.github.mlangc.wetterfrosch

import org.scalatest.FreeSpec
import org.scalatest.OptionValues

class HistoryExportDataTest extends FreeSpec with OptionValues {
  "a few sanity checks" in {
    val data = new HistoryExportData()
    val csv = data.csvDaily
    assert(csv.size > 12000)
    assert(csv.size < 15000)

    val firstRow = csv.head
    assert(firstRow.get(HistoryExportCols.Year).value == 1985.0)
    assert(firstRow.get(HistoryExportCols.Day).value == 1.0)
    assert(firstRow.get(HistoryExportCols.TempDailyMean).value == 0.31)
    assert(firstRow.get(HistoryExportCols.WindGustDailyMin).value == 12.6)

    val aug302018 = csv
      .find(r => r(HistoryExportCols.Year) == 2018 && r(HistoryExportCols.Month) == 8 && r(HistoryExportCols.Day) == 30)
      .value
    assert(aug302018(HistoryExportCols.WindGustDailyMin) == 9.36)

    val snowButNoPrecipitation = csv.filter { row =>
        val snow = row(HistoryExportCols.SnowfallAmountDaily)
        val precipitation = row(HistoryExportCols.TotalPrecipitationDailySum)
        snow > 0.85 && precipitation <= 0
      }.map { row =>
        (row(HistoryExportCols.Year),
          row(HistoryExportCols.Month),
          row(HistoryExportCols.Day),
          row(HistoryExportCols.TotalPrecipitationDailySum),
          row(HistoryExportCols.SnowfallAmountDaily))
      }

    assert(snowButNoPrecipitation.isEmpty)
  }
}
