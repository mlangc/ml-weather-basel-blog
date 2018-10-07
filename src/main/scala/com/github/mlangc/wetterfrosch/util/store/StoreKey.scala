package com.github.mlangc.wetterfrosch.util.store

case class StoreKey(prefix: Package, name: String)

object StoreKey {
  def apply(clazz: Class[_], name: String): StoreKey = StoreKey(clazz.getPackage, name)
}
