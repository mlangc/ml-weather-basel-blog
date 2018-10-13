package com.github.mlangc.wetterfrosch.smile.performance

import com.github.mlangc.wetterfrosch.smile.SmileLabModule
import org.scalameter.reporting.LoggingReporter
import org.scalameter.{Aggregator, Bench, Measurer, Persistor}

class SmileLocalBench extends Bench.LocalTime with SmileLabModule {
  override def persistor: Persistor = Persistor.None
  override def reporter = new LoggingReporter[Double]
  override def aggregator: Aggregator[Double] = Aggregator.average
  override def measurer: Measurer[Double] = new Measurer.Default

  protected def expandName(basename: String): String = {
    val dailyHourly = if (useHourlyData) "hourly" else "daily"
    s"$basename-$dailyHourly-$timeSeriesLen"
  }
}
