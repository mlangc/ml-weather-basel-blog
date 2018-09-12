package com.github.mlangc.wetterfrosch

import java.net.URL

object Resources {
  def historyExportCsv20180830: URL = {
    localResourcePathToUrl("/history_export_2018-08-30T19_17_45.csv")
  }

  private def localResourcePathToUrl(path: String): URL = {
    val url = getClass.getResource(path)
    require(url != null, s"Cannot load resource at $path")
    url
  }
}
