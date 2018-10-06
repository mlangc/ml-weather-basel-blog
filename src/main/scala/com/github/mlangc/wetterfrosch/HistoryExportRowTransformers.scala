package com.github.mlangc.wetterfrosch

import com.github.mlangc.wetterfrosch.ExportDataTransformations.cleanDailyData
import com.github.mlangc.wetterfrosch.ExportDataTransformations.cleanHourlyData
import com.github.mlangc.wetterfrosch.HistoryExportRowTransformers.RowTransformation

object HistoryExportRowTransformers {
  type RowTransformation = Map[String, Double] => Map[String, Double]
}

class HistoryExportRowTransformers(general: Seq[RowTransformation] = Seq(),
                                   cleanDaily: Seq[RowTransformation] = Seq(cleanDailyData),
                                   cleanHourly: Seq[RowTransformation] = Seq(cleanHourlyData)) {

  private val generalTransformation = Function.chain(general)
  private val dailyTransformation = Function.chain(cleanDaily).andThen(generalTransformation)
  private val hourlyTransformation = Function.chain(cleanHourly).andThen(generalTransformation)

  def transformDaily(row: Map[String, Double]): Map[String, Double] = {
    dailyTransformation(row)
  }

  def transformHourly(row: Map[String, Double]): Map[String, Double] = {
    hourlyTransformation(row)
  }

}
