package jfugue

import com.typesafe.scalalogging.StrictLogging

object KillingMeSoftlyChords extends App with PlayerProvider with StrictLogging{
  def strummingPattern(chord: String) = "i s s i i s s i".split(" ").map(chord + _).mkString(" ")
  val intro = "Amin"
  val verse = " Amin Cmaj Dmaj Fmaj Amin Cmaj Emaj Edom7 Amin Cmaj Dmaj Fmaj Amin Emaj Amin Emaj"
  val ending = " Amin"
  val chords = intro + verse * 4 + ending
  val staccato = "T[Andantino] " + chords.split(" ").map(strummingPattern).mkString(" ")
  logger.info(s"You are listening to '$staccato'")
  staccato.play
}