package com.github.mlangc.wetterfrosch

import java.net.URL

object Resources {
  def historyExportDaily: URL = {
    localResourcePathToUrl("/history_export_daily.csv")
  }

  private def localResourcePathToUrl(path: String): URL = {
    val url = getClass.getResource(path)
    require(url != null, s"Cannot load resource at $path")
    url
  }
}
