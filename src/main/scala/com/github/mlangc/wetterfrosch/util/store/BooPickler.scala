package com.github.mlangc.wetterfrosch.util.store

import java.nio.ByteBuffer

import boopickle.{Default, PickleState}

object BooPickler {
  implicit def adapter[T](implicit booState: PickleState,  booPickler: boopickle.Pickler[T]): Pickler[T] = new Pickler[T] {
    def toBytes(t: T): ByteBuffer = Default.Pickle.intoBytes(t)
    def fromBytes(bytes: ByteBuffer): T = Default.Unpickle[T].fromBytes(bytes)
  }
}
