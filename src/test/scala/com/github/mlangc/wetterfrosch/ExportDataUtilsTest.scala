package com.github.mlangc.wetterfrosch

import org.scalatest.FreeSpec

class ExportDataUtilsTest extends FreeSpec with ExportDataModule {
  private lazy val data = labeledDataAssembler.assemblyDailyData(3).take(4)

  "To and from doubles" - {
    "with sequence data" in {
      val keys = data.head.head.keySet
      val seq = data.head
      val flattenedSeq = ExportDataUtils.toDoubles(seq)
      val reconstructedSeq = ExportDataUtils.relabelFlattenedSeq(flattenedSeq, keys)
      assert(reconstructedSeq == seq)
    }
  }
}
