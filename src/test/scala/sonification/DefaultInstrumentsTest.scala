package sonification

import org.jfugue.player.Player

import sonification.music.MusicCommons

object DefaultInstrumentsTest extends App {
  val instruments = MusicCommons.JFugueInstrumentsStream.take(20)
  val ms = instruments.zipWithIndex map { case (i, idx) => s"V${idx % 5} I[$i] c d e f g a b" } mkString (" \n")
  println(ms)
  new Player().play(ms)
}