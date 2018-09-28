package com.github.mlangc.wetterfrosch

class LabeledDataAssembler(exportData: HistoryExportData) {
  def assembleHourlyData(step: Int, numSteps: Int): Seq[Seq[Map[String, Double]]] = {
    Seq()
  }

  def assemblyDailyData(daysBack: Int): Seq[Seq[Map[String, Double]]] = {
    exportData.csvDaily.sliding(daysBack + 1, 1).toSeq
  }
}
