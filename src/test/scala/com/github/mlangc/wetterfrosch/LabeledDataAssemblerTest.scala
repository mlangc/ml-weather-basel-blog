package com.github.mlangc.wetterfrosch

import java.time.LocalDate

import com.github.mlangc.wetterfrosch.HistoryExportCols.Hour
import com.github.mlangc.wetterfrosch.HistoryExportCols.MeanSealLevelPressure
import org.scalatest.FreeSpec
import org.scalatest.OptionValues

class LabeledDataAssemblerTest extends FreeSpec with ExportDataModule with OptionValues {
  "Hourly data" - {
    "1 step" - {
      val numSteps = 1

      "step size 1" in {
        checkedAssemble(numSteps, stepSize = 1)
      }

      "step size 2" in {
        checkedAssemble(numSteps, stepSize = 2)
      }
    }

    "3 steps" - {
      val numSteps = 3

      "step size 2" in {
        checkedAssemble(numSteps, stepSize = 2)
      }

      "step size 3" in {
        val labeledData = checkedAssemble(numSteps, stepSize = 3)
        val date = LocalDate.of(2018, 8, 30)
        val ex20180830 = labeledData.find(data => ExportDataUtils.localDateFrom(data.last) == date).value
        val temps = ex20180830.init.map(_(HistoryExportCols.Temp)).map(_.toInt)
        val ps = ex20180830.init.map(_(MeanSealLevelPressure)).map(_.toInt)

        assert(temps == Seq(25, 24, 21))
        assert(ps == Seq(1015, 1016, 1017))
      }
    }
  }

  private def checkedAssemble(numSteps: Int, stepSize: Int): Seq[Seq[Map[String, Double]]] = {
    val labeledData = labeledDataAssembler.assembleHourlyData(numSteps, stepSize)

    assert(labeledData.nonEmpty)
    assert(labeledData.filter(_.size != numSteps + 1).take(1).isEmpty)

    val dataWithDateTimeIconsistencies = {
      labeledData.filter { seq =>
        val hours = seq.init.map(_(Hour)).map(_.toInt)
        val hoursExpected = 0.until(numSteps).map(i => (hours.head + i*stepSize) % 24)

        if (hours != hoursExpected) {
          true
        } else {
          val labelDate = ExportDataUtils.localDateFrom(seq.last)
          val featuresDate = ExportDataUtils.localDateFrom(seq.init.last)

          labelDate.isBefore(featuresDate)
        }
      }
    }

    assert(dataWithDateTimeIconsistencies.take(1).isEmpty)

    val hoursBack = numSteps * stepSize
    val daysBack = hoursBack / 24 + 1
    val expectedSize = exportData.csvDaily.size - daysBack
    assert(labeledData.size == expectedSize)

    labeledData
  }

}
