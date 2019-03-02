package com.github.mlangc.wetterfrosch.util.tune

import scala.annotation.tailrec
import scala.util.Random

abstract class SettingsTuner[SettingsType](rng: Random) {
  def this(seed: Int) = this(new Random(seed))

  def tune(initialSettings: SettingsType, maxIterations: Int = 1000, maxRetries: Int = 15): Seq[(SettingsType, Double)] = {
    @tailrec
    def explore(explored: Map[SettingsType, Double], retries: Int = 0, iterations: Int = 0): Map[SettingsType, Double] = {
      if (iterations >= maxIterations || retries >= maxRetries) explored else {
        val (bestSetting, bestValue) = explored.minBy(_._2)
        onProgress(iterations, bestSetting, bestValue)

        val axis = rng.nextInt(numAxes)
        val settings = variationsAlongAxis(bestSetting, explored, axis)

        if (settings.isEmpty) {
          explore(explored, retries, iterations)
        } else {
          val settingsWithEvals = settings.par.map(s => s -> evalSettings(s))
          val (bestNewSettings, bestNewValue) = settingsWithEvals.minBy(_._2)

          val newRetries = {
            if (bestNewValue < bestValue) retries
            else retries + 1
          }

          val newExplored = explored + (bestNewSettings -> bestNewValue)
          explore(newExplored, newRetries, iterations + 1)
        }
      }
    }

    explore(Map(initialSettings -> evalSettings(initialSettings)))
      .keys
      .par
      .map(key => key -> evalSettings(key))
      .toSeq
      .seq
      .sortBy(_._2)
  }

  protected def numAxes: Int
  protected def variationsAlongAxis(setting: SettingsType, history: Map[SettingsType, Double], axis: Int): Seq[SettingsType]
  protected def evalSettings(settings: SettingsType): Double

  protected def onProgress(iteration: Int, bestSetting: SettingsType, bestValue: Double): Unit = {

  }
}
