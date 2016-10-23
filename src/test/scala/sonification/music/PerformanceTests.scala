package sonification.music

import java.io.File
import java.net.InetAddress
import java.util.concurrent.TimeUnit.{ MILLISECONDS, NANOSECONDS }

import org.scalatest.{ FunSuite, Matchers }

import sonification.controller.SettingsUtil
import sonification.openxes.MyXesParser

class PerformanceTests extends FunSuite with Matchers {

  /**
   * 1000,10 -> 900/1100
   * 500/1500 -> 1000,5
   */
  assume(InetAddress.getLocalHost().getHostName.toLowerCase == "z97")

  def time[R](maxDuration: Int, maxPlusMinus: Double = 0.1)(block: => R): R = {
    val t0 = System.nanoTime()
    val result = block // call-by-name
    val elapsed = System.nanoTime() - t0
    val elapsedMs = MILLISECONDS.convert(elapsed, NANOSECONDS)
    println(s"Elapsed time: $elapsedMs ms")
    elapsedMs.toInt should equal(maxDuration +- Math.round(maxDuration * maxPlusMinus).toInt)
    result
  }

  def staccato(file: String): String = {
    val xes = MyXesParser.parse(new File(file)).get
    val defaultSettings = SettingsUtil.defaultSettings(xes)
    StaccatoGenerator.makeStaccato(xes, defaultSettings).trim
  }

  test("repairExample.xes") {
    val stac = staccato("data/xes/repairExample.xes")
    val sequence = time(275922) { MusicCommons.sequence(stac) }
    time(40, 50) { MusicCommons.getMidi(sequence) }
  }

  test("generated_100k_50x2k.xes") {
    val stac = staccato("data/xes/generated_100k_50x2k.xes")
    val sequence = time(2950) { MusicCommons.sequence(stac) }
    time(40, 50) { MusicCommons.getMidi(sequence) }

  }
}