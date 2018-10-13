package com.github.mlangc.wetterfrosch

import java.time.LocalDate

trait LabeledDataFilter extends (Seq[Map[String, Double]] => Boolean) {

}

object LabeledDataFilter {
  val Default: LabeledDataFilter = rs => {
    val date = ExportDataUtils.localDateFrom(rs.last)
    date.isBefore(LocalDate.of(2018, 7, 31))
  }
}
