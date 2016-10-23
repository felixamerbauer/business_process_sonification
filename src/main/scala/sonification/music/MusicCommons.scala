package sonification.music

import java.io.{ ByteArrayInputStream, ByteArrayOutputStream, File }
import java.nio.file.Paths
import java.util.concurrent.TimeUnit

import scala.util.Try

import org.jfugue.midi.MidiDictionary
import org.jfugue.pattern.Pattern
import org.jfugue.player.{ Player, SequencerManager, SynthesizerManager }
import org.jfugue.theory.Note
import org.staccato.StaccatoParser

import com.typesafe.scalalogging.StrictLogging

import javax.sound.midi.{ Instrument => MidiInstrument, MidiSystem, Sequence }
import sonification.Metrics.{ TMC_duration, TMC_makeMidi, TMC_sequence, TMC_staccatoValid, t }
import sun.audio.{ AudioPlayer, AudioStream }

object MusicCommons extends StrictLogging {
  /** convenience class for microseconds */
  class Microsecond(val n: Long) extends AnyVal {

    /**
     * @return convert to milliseconds
     */
    def toMilliseconds = TimeUnit.MILLISECONDS.convert(n, TimeUnit.MICROSECONDS)
    def toMs = toMilliseconds
  }

  /** tries to determine list of available midi instruments  */
  lazy val MidiInstruments: Try[Seq[MidiInstrument]] = Try {
    val synthesizer = MidiSystem.getSynthesizer
    synthesizer.open()
    val instruments = synthesizer.getAvailableInstruments
    synthesizer.close()
    Seq(instruments: _*)
  }

  val JFugueInstrumentStrings = (0 to 127).map(e => MidiDictionary.INSTRUMENT_BYTE_TO_STRING.get(e.toByte)).toArray
  val JFugueInstrumentsStream = {
    val preselected = Array("Piano", "Celesta", "Violin", "Trumpet", "Overdriven_Guitar",
      "Electric_Piano_2", "Glockenspiel", "Tremolo_Strings", "Oboe", "Guitar_Harmonics",
      "Harpsichord", "Xylophone", "Pizzicato_Strings", "French_Horn", "Sawtooth",
      "Clavinet", "Tubular_Bells", "Orchestral_Strings", "Tenor_Sax", "Charang")
    val missing = (JFugueInstrumentStrings.toSet -- preselected.toSet).toArray
    val all = preselected ++ missing
    Stream from (0) map (e => all(e % all.length))
  }

  //  val Melodies = Array(
  //    "g5s b5s Rs Rs e6q",
  //    "e6s c6s Ri g5q",
  //    "g5s g5s g5s Rs g5q",
  //    "g5s Rs c6i Rs g5q",
  //    "e3h")
  val Melodies = Array(
    "c5maj", "d5min", "e5min", "f5maj", "g5maj", "a5min", "b5dim",
    "c6maj", "d6min", "e6min", "f6maj", "g6maj", "a6min", "b6dim",
    "c7maj", "d7min", "e7min", "f7maj", "g7maj", "a7min", "b7dim")
  val MelodiesStream = Stream.from(0).map(e => Melodies(e % Melodies.length))

  val Drums = Note.PERCUSSION_NAMES
  val DrumStream = Stream.from(0).map(e => Drums(e % Drums.length))

  val Rhythms = Array("q q", "i i i i", "s s s s s s s s",
    "q i i", "i i q", "i q i",
    "q s s i", "s q s i", "s s q i", "s s i q",
    "s s i s s i", "i s s i s s", "s i s s i s", "i i s s s s", "i s s s s i")
  val RhythmsStream = Stream.from(0).map(e => Rhythms(e % Rhythms.length))

  /**
   * calculate length of music string
   * @param staccato
   * @return length in microseconds
   */
  def duration(staccato: String): Microsecond = duration(new Pattern(staccato))

  def duration(sequence: Sequence): Microsecond = new Microsecond(sequence.getMicrosecondLength())
  /**
   * Check if a staccato is valid
   * @param staccato
   * @return validity
   */
  def melodyStaccatoValid(staccato: String): Boolean = t(TMC_staccatoValid) {
    // TODO new parser
    Try(new StaccatoParser().parse(new Pattern(staccato))).isSuccess
  }

  def rhythmStaccatoValid(staccato: String): Boolean = t(TMC_staccatoValid) {
    // TODO new parser
    Try(new StaccatoParser().parse(new Pattern(staccato))).isSuccess
  }

  /**
   * calculate length of music string pattern
   * @param staccato
   * @return length in microseconds
   */
  def duration(pattern: Pattern): Microsecond = t(TMC_duration) {
    val player = new Player()
    val sequence = player.getSequence(pattern)
    new Microsecond(sequence.getMicrosecondLength())
  }

  /**
   * Convert a jfugue music string in a javax sequence object
   * @param staccato
   * @return sequence
   */
  def sequence(staccato: String): Sequence = t(TMC_sequence) {
    logger.debug(s"Sequence for staccato ${staccato.take(100)}")
    val player = new Player()
    val pattern = new Pattern(staccato)
    player.getSequence(pattern)
  }

  def playVolumeSound(volume: Int) {
    val ms = StaccatoGenerator.volume(volume) + "c"
    val pw = new PlayerWrapper()
    pw.init(sequence(ms))
    pw.resume
  }

  def playBalanceSound(balance: Int) {
    val ms = StaccatoGenerator.balance(balance) + "c"
    val pw = new PlayerWrapper()
    pw.init(sequence(ms))
    pw.resume
  }

  /**
   * TODO
   * @param instrument
   * @param melody
   * @return
   */
  def staccato(instrument: String, melody: String): String = s"I[$instrument] $melody"

  def staccatoDrumRhythm(drum: String, rhtyhm: String): String = {
    rhtyhm.split(" ").map(e => s"[$drum]$e").mkString(" ")
  }

  /**
   * TODO
   * @param sequence
   * @return midi byte array
   */
  def getMidi(sequence: Sequence): Array[Byte] = t(TMC_makeMidi) {
    val baos = new ByteArrayOutputStream()
    MidiSystem.write(sequence, 1, baos)
    baos.toByteArray()
  }

  /**
   * TODO
   * @param instrument
   */
  def instrumentSample(instrument: String) {
    val ms = s"I[${instrument}] c d e f g a b c6"
    logger.debug(s"instrumentSample  $ms")
    playAsWav(ms)
  }

  def drumSample(drum: String) {
    val ms = s"V[Percussion] [${drum}] q i i q i i"
    logger.debug(s"instrumentSample  $ms")
    playAsWav(ms)
  }

  /**
   * TODO add instrument
   * @param melody
   * @param speed
   */
  def melodySample(melody: String, speed: Double) {
    val ms = s" I[Piano] ${StaccatoGenerator.speed(speed)} $melody"
    logger.debug(s"melodySample  $ms")
    playAsWav(ms)
  }

  def rhythmSample(rhythm: String, speed: Double) {
    val ms = s"${StaccatoGenerator.speed(speed)} V[Percussion] " + rhythm.split(" ").map(e => s"[Hi_Bongo]$e").mkString(" ")
    logger.debug(s"rhythmSample  $ms")
    playAsWav(ms)
  }

  def fileSize(duration: Long): String = {

    // http://stackoverflow.com/questions/3758606/how-to-convert-byte-size-into-human-readable-format-in-java
    def humanReadableByteCount(bytes: Long, si: Boolean): String = {
      val unit = if (si) 1000 else 1024
      if (bytes < unit) {
        bytes + " B"
      } else {
        val exp = (Math.log(bytes) / Math.log(unit)).toInt
        val pre = (if (si) "kMGTPE" else "KMGTPE").charAt(exp - 1) + (if (si) "" else "i")
        f"${bytes / Math.pow(unit, exp)}%.1f $pre%sB"
      }
    }
    val sizeBytes = duration / 1000 * 44100 * 2 * 2
    humanReadableByteCount(sizeBytes, false)
  }

  def playAsWav(staccato: String) = {
    logger.debug(s"playAsWav $staccato")
    val seq = new Player().getSequence(staccato)
    val baos = new ByteArrayOutputStream()
    Midi2WavRenderS.render(null, seq, baos)
    val bytes = baos.toByteArray()
    val is = new ByteArrayInputStream(bytes)
    val audioStream = new AudioStream(is)
    // TODO run on seperate thread
    AudioPlayer.player.start(audioStream)
  }

  def changeSoundbank() {
    val workingDir = new File(Paths.get("").toFile.getAbsolutePath())
    val wdirPath = workingDir.getAbsolutePath
    logger.debug(s"Looking for sf2 files in $wdirPath")
    val parent = workingDir.getParentFile
    println("parent " + parent)
    assert(workingDir.isDirectory(), "working dir is not an actual directory")
    val sf2s = workingDir.listFiles().filter(e => e.getName.endsWith(".sf2") && e.isFile)
    val sf2 = sf2s match {
      case Array() =>
        logger.warn(s"No sf2 sound file in $wdirPath")
        None
      case Array(single) =>
        logger.debug(s"Using single sf2 sound bank")
        Some(single)
      case many =>
        logger.debug("using biggest non-default sf2 sound bank")
        // don't use the default one (if there) and take the biggest from the rest
        many.filterNot(_.getName == "GeneralUser GS v1.47.sf2").sortBy(_.length()).lastOption
    }
    sf2 foreach changeSoundbank
  }

  def changeSoundbank(file: File) {
    logger.info(s"Starting changing soundbank to ${file.getAbsolutePath}")
    val synth = MidiSystem.getSynthesizer
    synth.open()
    val sbDefault = synth.getDefaultSoundbank
    synth.unloadAllInstruments(sbDefault)
    val sbDeluxe = MidiSystem.getSoundbank(file)
    synth.loadAllInstruments(sbDeluxe)
    // change the JFugue synthesizer
    val customJFugue = SynthesizerManager.getInstance.setSynthesizer(synth)
    // connect the JFugue sequencer to the new synthesizer
    SequencerManager.getInstance.connectSequencerToSynthesizer()
    logger.info("Changing soundbank done")
  }
}