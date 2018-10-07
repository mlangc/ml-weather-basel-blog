package com.github.mlangc.wetterfrosch.util.store

import java.io.File
import java.nio.file.Files

import org.apache.commons.io.FileUtils
import org.scalatest.FreeSpec

import boopickle.Default._
import BooPickler.adapter

class FsBasedObjectStoreTest extends FreeSpec {
  "Load and store" - {
    "a double" in {
      doWithObjectStore { store =>
        assert(store.get[Double](StoreKey(getClass, "d1")).isEmpty)
        assert(store.load(StoreKey(getClass, "d1"))(42.0) == 42.0)
        assert(store.get[Double](StoreKey(getClass, "d1")).contains(42.0))

        assert(store.get[Double](StoreKey(getClass, "d2")).isEmpty)
        assert(store.get[Double](StoreKey(classOf[String], "d1")).isEmpty)
      }
    }

    "a case class" in {
      doWithObjectStore { store =>
        case class CaseClass(a: Int, b: String, c: Seq[Double])
        val obj = CaseClass(3, "3", Seq(3.3, 4.4))

        assert(store.get[CaseClass](StoreKey(getClass, "c")).isEmpty)
        assert(store.load(StoreKey(getClass, "c"))(obj) == obj)
        assert(store.get[CaseClass](StoreKey(getClass, "c")).contains(obj))
      }
    }
  }

  private def doWithObjectStore(op: ObjectStore => Unit): Unit = {
    doWithTmpDir { tmpDir =>
      val objectStore = new FsBasedObjectStore(tmpDir)
      op(objectStore)
    }
  }

  private def doWithTmpDir[T](op: File => T): T = {
    val tmpDir = Files.createTempDirectory("test-").toFile
    try {
      op(tmpDir)
    } finally {
      FileUtils.deleteDirectory(tmpDir)
    }
  }
}
