package com.github.mlangc.wetterfrosch.util

import java.io.File

import com.github.mlangc.wetterfrosch.util.plot.PlotUtils
import com.github.mlangc.wetterfrosch.util.store.{FsBasedObjectStore, ObjectStore}
import com.github.mlangc.wetterfrosch.util.tune.TuningHelpers
import com.softwaremill.macwire.wire

trait UtilityModule extends RandomModule {
  lazy val objectStore: ObjectStore = {
    val storeDir = new File(System.getProperty("user.home"), "tmp/ml-weather-basel-blog/store")
    new FsBasedObjectStore(storeDir)
  }

  lazy val plotUtils: PlotUtils = wire[PlotUtils]
  lazy val tuningHelpers = wire[TuningHelpers]
}
