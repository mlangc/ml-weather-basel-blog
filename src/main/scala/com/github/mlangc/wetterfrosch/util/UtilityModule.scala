package com.github.mlangc.wetterfrosch.util

import java.io.File

import com.github.mlangc.wetterfrosch.util.store.{FsBasedObjectStore, ObjectStore}

trait UtilityModule {
  lazy val objectStore: ObjectStore = {
    val storeDir = new File(System.getProperty("user.home"), "tmp/ml-weather-basel-blog/store")
    new FsBasedObjectStore(storeDir)
  }
}
