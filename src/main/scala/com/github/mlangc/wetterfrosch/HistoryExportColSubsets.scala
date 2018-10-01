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

  val ColsForLast3DaysFromTree100 = Seq(
    Set("Wind Direction daily mean [80 m above gnd]", "Sunshine Duration daily sum [sfc]", "Wind Gust daily max [sfc]", "Wind Direction daily mean [10 m above gnd]", "Wind Direction daily mean [900 mb]", "Wind Speed daily min [900 mb]", "Wind Speed daily mean [900 mb]", "Wind Speed daily max [900 mb]", "Total Precipitation daily sum [sfc]", "High Cloud Cover daily mean [high cld lay]", "Wind Speed daily mean [80 m above gnd]", "Mean Sea Level Pressure daily max [MSL]", "Wind Speed daily mean [10 m above gnd]", "Temperature daily mean [2 m above gnd]", "Wind Speed daily max [80 m above gnd]"),
    Set("Wind Direction daily mean [80 m above gnd]", "Wind Gust daily max [sfc]", "Month", "Relative Humidity daily min [2 m above gnd]", "Wind Direction daily mean [10 m above gnd]", "Medium Cloud Cover daily min [mid cld lay]", "Wind Direction daily mean [900 mb]", "Wind Speed daily min [900 mb]", "Low Cloud Cover daily mean [low cld lay]", "Low Cloud Cover daily max [low cld lay]", "Total Precipitation daily sum [sfc]", "Wind Speed daily max [10 m above gnd]", "Shortwave Radiation daily sum [sfc]", "Wind Speed daily mean [80 m above gnd]", "Wind Speed daily min [10 m above gnd]", "Mean Sea Level Pressure daily min [MSL]", "Temperature daily mean [2 m above gnd]", "Wind Speed daily max [80 m above gnd]", "Temperature daily min [2 m above gnd]", "Relative Humidity daily mean [2 m above gnd]", "Medium Cloud Cover daily mean [mid cld lay]"),
    Set("Day", "Wind Gust daily max [sfc]", "Temperature daily max [2 m above gnd]", "Relative Humidity daily min [2 m above gnd]", "Wind Direction daily mean [10 m above gnd]", "Wind Direction daily mean [900 mb]", "Wind Speed daily min [900 mb]", "Wind Speed daily mean [900 mb]", "Wind Gust daily min [sfc]", "Wind Speed daily max [900 mb]", "Total Cloud Cover daily mean [sfc]", "Mean Sea Level Pressure daily mean [MSL]", "Total Precipitation daily sum [sfc]", "High Cloud Cover daily mean [high cld lay]", "Mean Sea Level Pressure daily max [MSL]", "Wind Speed daily mean [10 m above gnd]", "Mean Sea Level Pressure daily min [MSL]", "Temperature daily min [2 m above gnd]", "Relative Humidity daily mean [2 m above gnd]", "Medium Cloud Cover daily max [mid cld lay]", "Medium Cloud Cover daily mean [mid cld lay]")
  )

  val ColsForLast9DaysFromTree100 = Seq(
    Set("Wind Gust daily max [sfc]", "Wind Direction daily mean [10 m above gnd]", "Wind Direction daily mean [900 mb]", "Wind Speed daily mean [900 mb]", "Low Cloud Cover daily mean [low cld lay]", "Mean Sea Level Pressure daily max [MSL]"),
    Set("Wind Direction daily mean [80 m above gnd]", "Wind Gust daily max [sfc]", "Wind Direction daily mean [10 m above gnd]", "Wind Speed daily max [900 mb]", "Shortwave Radiation daily sum [sfc]", "Mean Sea Level Pressure daily max [MSL]", "Temperature daily mean [2 m above gnd]"),
    Set("Wind Speed daily min [900 mb]", "Medium Cloud Cover daily max [mid cld lay]", "Wind Speed daily mean [10 m above gnd]", "Medium Cloud Cover daily mean [mid cld lay]"),
    Set("Temperature daily max [2 m above gnd]", "Wind Direction daily mean [10 m above gnd]", "Wind Speed daily max [900 mb]", "Mean Sea Level Pressure daily mean [MSL]", "Mean Sea Level Pressure daily min [MSL]"),
    Set("Wind Speed daily min [80 m above gnd]", "Wind Direction daily mean [10 m above gnd]", "Wind Gust daily mean [sfc]", "Wind Speed daily mean [900 mb]", "Wind Gust daily min [sfc]", "Mean Sea Level Pressure daily mean [MSL]", "Wind Speed daily min [10 m above gnd]", "Medium Cloud Cover daily mean [mid cld lay]"),
    Set("Wind Speed daily min [80 m above gnd]", "Mean Sea Level Pressure daily mean [MSL]", "Shortwave Radiation daily sum [sfc]", "Mean Sea Level Pressure daily max [MSL]", "Total Cloud Cover daily min [sfc]", "Wind Speed daily max [80 m above gnd]"),
    Set("Relative Humidity daily min [2 m above gnd]", "Total Precipitation daily sum [sfc]", "Mean Sea Level Pressure daily mean [MSL]", "Shortwave Radiation daily sum [sfc]"),
    Set("Wind Direction daily mean [900 mb]", "Low Cloud Cover daily max [low cld lay]", "Total Precipitation daily sum [sfc]", "Wind Speed daily max [10 m above gnd]", "Shortwave Radiation daily sum [sfc]", "Wind Speed daily mean [80 m above gnd]", "Mean Sea Level Pressure daily min [MSL]", "Wind Speed daily max [80 m above gnd]", "Medium Cloud Cover daily mean [mid cld lay]"),
    Set("High Cloud Cover daily max [high cld lay]", "Wind Direction daily mean [80 m above gnd]", "Wind Speed daily min [80 m above gnd]", "Wind Gust daily max [sfc]", "Temperature daily max [2 m above gnd]", "Relative Humidity daily min [2 m above gnd]", "Wind Direction daily mean [10 m above gnd]", "Medium Cloud Cover daily min [mid cld lay]", "Wind Direction daily mean [900 mb]", "Wind Speed daily min [900 mb]", "Low Cloud Cover daily mean [low cld lay]", "High Cloud Cover daily min [high cld lay]", "Mean Sea Level Pressure daily mean [MSL]", "Total Precipitation daily sum [sfc]", "High Cloud Cover daily mean [high cld lay]", "Wind Speed daily max [10 m above gnd]", "Low Cloud Cover daily min [low cld lay]", "Wind Speed daily min [10 m above gnd]", "Wind Speed daily mean [10 m above gnd]", "Relative Humidity daily max [2 m above gnd]", "Mean Sea Level Pressure daily min [MSL]", "Wind Speed daily max [80 m above gnd]", "Temperature daily min [2 m above gnd]", "Relative Humidity daily mean [2 m above gnd]", "Medium Cloud Cover daily mean [mid cld lay]"),
  )
}
