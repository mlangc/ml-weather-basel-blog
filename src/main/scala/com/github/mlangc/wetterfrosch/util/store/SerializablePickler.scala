package com.github.mlangc.wetterfrosch.util.store
import java.io.{ByteArrayInputStream, ByteArrayOutputStream, ObjectInputStream, ObjectOutputStream}
import java.nio.ByteBuffer

class SerializablePickler[T <: java.io.Serializable] extends Pickler[T] {
  def toBytes(t: T): ByteBuffer = {
    val bout = new ByteArrayOutputStream()
    val oout = new ObjectOutputStream(bout)
    oout.writeObject(t)
    oout.close()
    ByteBuffer.wrap(bout.toByteArray)
  }

  def fromBytes(bytes: ByteBuffer): T = {
    val bin = new ByteArrayInputStream(bytes.array())
    val oin = new ObjectInputStream(bin)
    try {
      oin.readObject().asInstanceOf[T]
    } finally {
      oin.close()
    }
  }
}
