package sonification.music

import org.jfugue.pattern.Pattern
import org.jfugue.rhythm.Rhythm

object RepeatMetronom extends App {
  // pattern repeat
  val patternMain = new Pattern("c d e f g a b")
  val patternMetronome = new Pattern("V10 I[Woodblock]").repeat(4)
  val total = patternMain.add(patternMetronome)

  println(s"pattern repeat $total")

  // rhythm
  val rhythm = new Rhythm()
    .addLayer("O..oO...O..oOO..") // This is Layer 0
    .addLayer("..S...S...S...S.")
    .addLayer("````````````````")
    .addLayer("...............+") // This is Layer 3
    .addOneTimeAltLayer(3, 3, "...+...+...+...+") // Replace Layer 3 with this string on the 4th (count from 0) measure
    .setLength(4)
  val patternRhytm = rhythm.getPattern

  println(s"pattern $patternRhytm")
}