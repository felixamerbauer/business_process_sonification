package sonification.music

import java.io.File
import java.util.concurrent.TimeUnit

import org.jfugue.midi.MidiFileManager
import org.jfugue.parser.ParserListener
import org.jfugue.pattern.Pattern
import org.jfugue.player.Player
import org.jfugue.theory.{ Chord, Note }
import org.staccato.StaccatoParser

import com.typesafe.scalalogging.StrictLogging

import javax.sound.midi.{ Instrument, Sequence }

object Helper extends App with StrictLogging {
  def print(i: Instrument) = s"${i.getName()} ${i.getSoundbank().getName()} ${i.getPatch().getBank()} ${i.getPatch().getProgram()}"

  val instruments = MusicCommons.MidiInstruments
  logger.info(instruments.get.map(print).mkString("\n"))

}

object MidiPlayground extends App with StrictLogging {
  val p = new Player()
  val staccato = "C D E F G A B C"
  val sequence = p.getSequence(new Pattern(staccato))
  MidiFileManager.save(sequence, new File("tmp/test.midi"))
  p.play(staccato)
}

object PlayCMajorScale extends App with StrictLogging {
  val p = new Player()
  logger.info(MidiFileManager.loadPatternFromMidi(new File("tmp/C-Dur_Tonleiter.mid")).toString())
  logger.info(MidiFileManager.loadPatternFromMidi(new File("tmp/test.midi")).toString())
}

class MyDurationPatternTool extends ParserListener with StrictLogging {

  private var activeVoice: Byte = 0

  private var voiceDuration = Array[Double]()

  reset()

  override def onTrackChanged(track: Byte) {
    logger.info(s"onTrackChanged $track")
    activeVoice = track
  }

  override def onNoteParsed(note: Note) {
    logger.info(s"noteEvent $note with duration ${note.getDuration}")
    val duration = note.getDuration
    voiceDuration(activeVoice) += duration
  }

  def reset() {
    logger.info("reset")
    voiceDuration = Array.fill[Double](16)(0D)
  }

  def getDuration(): Double = {
    logger.info("getDuration")
    voiceDuration.max
  }

  def afterParsingFinished(): Unit = {
    ???
  }

  def beforeParsingStarts(): Unit = {
    ???
  }

  def onBarLineParsed(x$1: Long): Unit = {
    ???
  }

  def onChannelPressureParsed(x$1: Byte): Unit = {
    ???
  }

  def onChordParsed(x$1: Chord): Unit = {
    ???
  }

  def onControllerEventParsed(x$1: Byte, x$2: Byte): Unit = {
    ???
  }

  def onLayerChanged(x$1: Byte): Unit = {
    ???
  }

  def onFunctionParsed(x$1: String, x$2: Any): Unit = {
    ???
  }

  def onLyricParsed(x$1: String): Unit = {
    ???
  }

  def onInstrumentParsed(x$1: Byte): Unit = {
    ???
  }

  def onKeySignatureParsed(x$1: Byte, x$2: Byte): Unit = {
    ???
  }

  def onMarkerParsed(x$1: String): Unit = {
    ???
  }

  def onPitchWheelParsed(x$1: Byte, x$2: Byte): Unit = {
    ???
  }

  def onPolyphonicPressureParsed(x$1: Byte, x$2: Byte): Unit = {
    ???
  }

  def onSystemExclusiveParsed(x$1: Byte*): Unit = {
    ???
  }

  def onTempoChanged(x$1: Int): Unit = {
    ???
  }

  def onTimeSignatureParsed(x$1: Byte, x$2: Byte): Unit = {
    ???
  }

  def onTrackBeatTimeBookmarkRequested(x$1: String): Unit = {
    ???
  }

  def onTrackBeatTimeBookmarked(x$1: String): Unit = {
    ???
  }

  def onTrackBeatTimeRequested(x$1: Double): Unit = {
    ???
  }

  def onNotePressed(x$1: Note): Unit = {
    ???
  }

  def onNoteReleased(x$1: Note): Unit = {
    ???
  }
}

object DurationPattern extends App with StrictLogging {
  val parser = new StaccatoParser()
  val durationPatternTool = new MyDurationPatternTool()
  parser.addParserListener(durationPatternTool)
  parser.parse(new Pattern("T[Allegro] C5w D5h E5q F5i G5s A5t B5x C6w"))
  logger.info(durationPatternTool.getDuration().toString)
}

object DurationViaMidiSequence extends App with StrictLogging {
  val p = new Player()
  //  val staccato = "T[Allegro] C5q"
  //  val staccato = "T[Adagio] C5q"
  val staccato = "T[Grave] C5q"
  val sequence = p.getSequence(new Pattern(staccato))
  val lengthMicroseconds = sequence.getMicrosecondLength()
  logger.info(s"sequence DivisionType ${sequence.getDivisionType()}, TickLength ${sequence.getTickLength()}, Resolution ${sequence.getResolution()}")
  val lengthMilliseconds = TimeUnit.MILLISECONDS.convert(lengthMicroseconds, TimeUnit.MICROSECONDS)
  logger.info(s"$lengthMicroseconds microseconds ($lengthMilliseconds ms)")
}

object DurationViaMidiFile extends App with StrictLogging {
  val p = new Player()
  //  val staccato = "T[Allegro] C5w"
  val staccato = "T[Adagio] C5q"
  val sequence: Sequence = p.getSequence(new Pattern(staccato))
  val lengthMicroseconds = sequence.getMicrosecondLength()
  val lengthMilliseconds = TimeUnit.MILLISECONDS.convert(lengthMicroseconds, TimeUnit.MICROSECONDS)
  logger.info(s"$lengthMicroseconds microseconds ($lengthMilliseconds ms)")
}

object StartAtExamples extends App with StrictLogging {
  val ms = "Cq @100 Cq"
  logger.info(MusicCommons.duration(ms).toMilliseconds.toString)
}