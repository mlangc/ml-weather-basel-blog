package com.github.mlangc.wetterfrosch.util.store

import java.nio.ByteBuffer

import boopickle.{Default, PickleState}
import com.github.mlangc.wetterfrosch.SingleValueRegressionEvaluation

object DefaultPicklers {
  implicit def boopickleAdapter[T](implicit booState: PickleState, booPickler: boopickle.Pickler[T]): Pickler[T] = new Pickler[T] {
    def toBytes(t: T): ByteBuffer = Default.Pickle.intoBytes(t)
    def fromBytes(bytes: ByteBuffer): T = Default.Unpickle[T].fromBytes(bytes)
  }

  implicit def singleRegresionValueEvaluationPickler: Pickler[SingleValueRegressionEvaluation] = new Pickler[SingleValueRegressionEvaluation] {
    import boopickle.Default._

    def toBytes(t: SingleValueRegressionEvaluation): ByteBuffer = {
      Default.Pickle.intoBytes((t.mse, t.mae))
    }

    def fromBytes(bytes: ByteBuffer): SingleValueRegressionEvaluation = {
      val (mse, mae) = Default.Unpickle[(Double, Double)].fromBytes(bytes)
      SingleValueRegressionEvaluation(mse = mse, mae = mae)
    }
  }
}
