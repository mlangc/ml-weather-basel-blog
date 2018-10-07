package com.github.mlangc.wetterfrosch.util.store

import java.nio.ByteBuffer

trait Pickler[T] {
  def toBytes(t: T): ByteBuffer
  def fromBytes(bytes: ByteBuffer): T
}
