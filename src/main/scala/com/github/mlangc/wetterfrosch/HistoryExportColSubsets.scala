package com.github.mlangc.wetterfrosch

object HistoryExportColSubsets {
  val ColsFromLastDayForTree23 = Set(
    HistoryExportCols.TotalPrecipitationDailySum,
    HistoryExportCols.WindDirectionDailyMean900mb,
    HistoryExportCols.MeanSeaLevelPressureDailyMin,
    HistoryExportCols.HighCloudCoverDailyMean,
    HistoryExportCols.WindSpeedDailyMin900mb,
    HistoryExportCols.TempDailyMax,
    HistoryExportCols.MediumCloudCoverDailyMean,
    HistoryExportCols.WindSpeedDailyMin10m,
    HistoryExportCols.TotalCloudCoverDailyMean,
    HistoryExportCols.RelativeHumidityDailyMean,
    HistoryExportCols.WindGustDailyMean,
    HistoryExportCols.SunshineDurationDailySum,
    HistoryExportCols.WindSpeedDailyMax900mb,
    HistoryExportCols.WindDirectionDailyMean900mb
  )

  val ColsFromLastDayForTree4 = Set(
    HistoryExportCols.HighCloudCoverDailyMean,
    HistoryExportCols.MeanSeaLevelPressureDailyMin,
    HistoryExportCols.TotalPrecipitationDailySum
  )
}
