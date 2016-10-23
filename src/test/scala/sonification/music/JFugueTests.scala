package sonification.music

import java.io.File

import org.jfugue.midi.MidiFileManager
import org.jfugue.pattern.Pattern
import org.jfugue.player.Player

import com.typesafe.scalalogging.StrictLogging

object Voices extends App {
  val ms = "V1 c5 R e R g R c6 V2 R d R f R a"
  new Player().play(ms)
}

object VoicesMidi extends App {
  val ms = "V1 c R e R g R c6 V2 R d R f R a"
  val p = new Player()
  val sequence = p.getSequence(new Pattern(ms))
  MidiFileManager.save(sequence, new File("tmp/voices.midi"))
}

object Metronom extends App with StrictLogging {
  val ms = (0 to 11523 % 1000).
    map(sec => s"@${Math.round(sec * 1000 / 16.525D)} [Hi_Bongo]q").
    mkString("V9 ", " ", "")
  logger.debug(ms)
  new Player().play(ms)
}

