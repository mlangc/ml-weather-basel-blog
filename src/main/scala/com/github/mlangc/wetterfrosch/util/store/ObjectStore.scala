package com.github.mlangc.wetterfrosch.util.store

import java.nio.ByteBuffer

trait ObjectStore {
  def put[T](key: StoreKey, t: T)(implicit pickler: Pickler[T]): Unit = {
    putBytes(key, pickler.toBytes(t))
  }

  def get[T](key: StoreKey)(implicit pickler: Pickler[T]): Option[T] = {
    getBytes(key).map(pickler.fromBytes)
  }

  def load[T](key: StoreKey)(t: => T)(implicit pickler: Pickler[T]): T = {
    get(key).getOrElse {
      val tt = t
      put(key, tt)
      tt
    }
  }

  protected def putBytes(key: StoreKey, bytes: ByteBuffer): Unit
  protected def getBytes(key: StoreKey): Option[ByteBuffer]
}
