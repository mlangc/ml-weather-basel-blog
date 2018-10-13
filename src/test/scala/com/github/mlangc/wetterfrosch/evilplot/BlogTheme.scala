package com.github.mlangc.wetterfrosch.evilplot

import com.cibo.evilplot.plot.aesthetics.{DefaultTheme, Theme}

object BlogTheme {
  implicit val blogTheme: Theme = {
    import DefaultTheme.DefaultFonts

    DefaultTheme.defaultTheme.copy(
      fonts = DefaultFonts.copy(
        labelSize = addPercent(DefaultFonts.labelSize, 25),
        facetLabelSize = addPercent(DefaultFonts.facetLabelSize, 100),
        legendLabelSize = addPercent(DefaultFonts.legendLabelSize, 50),
        tickLabelSize = addPercent(DefaultFonts.tickLabelSize, 20)
      )
    )
  }

  private def addPercent(size: Double, p: Double): Double = {
    math.round(size * (1 + p/100))
  }
}
