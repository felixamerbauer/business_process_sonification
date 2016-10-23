package sonification.music

import java.io.{ ByteArrayOutputStream, File, FileOutputStream }

import org.jfugue.pattern.Pattern
import org.jfugue.player.Player

import com.typesafe.scalalogging.StrictLogging

import javax.sound.midi.MidiSystem

object TestVolume extends App {
  // 0 to 127 see http://www.blitter.com/~russtopia/MIDI/~jglatt/tech/midispec/vol.htm
  // src/org/jfugue/JFugueDefinitions.java
  new Player().play("X[VOLUME_COARSE]=10 c X[VOLUME_COARSE]=30 d X[VOLUME_COARSE]=50 e X[VOLUME_COARSE]=70 f X[VOLUME_COARSE]=90 g X[VOLUME_COARSE]=110 a X[VOLUME_COARSE]=127 b c")
}

object TestBalance extends App {
  // 0 to 127, left to right, see http://www.blitter.com/~russtopia/MIDI/~jglatt/tech/midispec/balance.htm  
  // src/org/jfugue/JFugueDefinitions.java
  new Player().play("X[BALANCE_COARSE]=0 c X[BALANCE_COARSE]=30 d X[BALANCE_COARSE]=50 e X[BALANCE_COARSE]=70 f X[BALANCE_COARSE]=90 g X[BALANCE_COARSE]=110 a X[BALANCE_COARSE]=127 b c")
}

object TestMidiSaving extends App with StrictLogging{
  val player = new Player()
  val pattern = new Pattern("c d e f g a b c")
  val sequence = player.getSequence(pattern)
  // TODO returns 0 and 1, what does that mean
  logger.info(MidiSystem.getMidiFileTypes().mkString("\n"))
  // TODO returns 1, what does that mean
  logger.info(MidiSystem.getMidiFileTypes(sequence).mkString("\n"))
  MidiSystem.write(sequence, 1, new File("tmp1.midi"))
  val baos = new ByteArrayOutputStream()
  MidiSystem.write(sequence, 1, baos)
  val data = baos.toByteArray()
  val fos = new FileOutputStream(new File("tmp2.midi"))
  fos.write(data)
  fos.close()
}

object TestMidiSynthesizer extends App with StrictLogging {
  val synthesizer = MidiSystem.getSynthesizer()
  logger.info(synthesizer.toString)
}
