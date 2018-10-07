package com.github.mlangc.wetterfrosch.util.store

import java.io.{File, IOException}
import java.net.URLEncoder
import java.nio.ByteBuffer
import java.nio.file.{Files, StandardCopyOption}

import org.apache.commons.io.FileUtils

class FsBasedObjectStore(path: File) extends ObjectStore {
  protected def putBytes(key: StoreKey, bytes: ByteBuffer): Unit = {
    val file = fileFromKey(key)
    val dir = file.getParentFile

    FileUtils.forceMkdir(dir)
    val tmp = Files.createTempFile(dir.toPath, "obj", ".tmp").toFile
    try {
      FileUtils.writeByteArrayToFile(tmp, bytes.array())
      Files.move(tmp.toPath, file.toPath, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING)
    } finally {
      if (tmp.exists()) {
        tmp.delete()
      }
    }
  }

  protected def getBytes(key: StoreKey): Option[ByteBuffer] = {
    val file = fileFromKey(key)
    try {
      Some(ByteBuffer.wrap(FileUtils.readFileToByteArray(file)))
    } catch {
      case _: IOException => None
    }
  }

  private def fileFromKey(key: StoreKey): File = {
    val dir = new File(path, key.prefix.getName.replace(".", File.separator))
    new File(dir, URLEncoder.encode(key.name, "UTF-8"))
  }
}
