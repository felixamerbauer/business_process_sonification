package sonification

import org.jfugue.player.Player

import sonification.music.MusicCommons.Melodies

object DefaultMelodiesTester extends App {
  val ms = Melodies.mkString(" Rh\n")
  println(ms)
  new Player().play(ms)
}