package sonification.music

import scala.io.{ Codec, Source }

import org.jfugue.player.Player

object PlayStaccatoFile extends App {
  val staccato = Source.fromFile("data/xes/repairExample.staccato")(Codec.UTF8).mkString
  new Player().play(staccato)
}