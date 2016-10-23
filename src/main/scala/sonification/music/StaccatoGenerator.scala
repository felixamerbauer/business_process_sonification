package sonification.music

import scala.annotation.migration
import scala.collection.JavaConversions.asScalaBuffer
import org.deckfour.xes.model.{ XElement, XLog }
import com.typesafe.scalalogging.StrictLogging
import sonification.Metrics.{ TSG_makeStaccato, TSG_musicEvents, TSG_staccato, t }
import sonification.controller.{ Event, InstrumentMapping, MappingCategory, MappingCategoryApplication, MelodyMapping, PanningMapping, PanningType, Settings, VolumeMapping, VolumeType }
import sonification.openxes.OpenXESHelper
import sonification.openxes.OpenXESHelper.RichLog
import sonification.ui.right.Player.DefaultSpeed
import sonification.controller.DrumMapping
import sonification.controller.RhythmMapping

object StaccatoGenerator extends StrictLogging {

  class MusicEvent(val staccato: Either[String, String], val volume: Option[Int], val panning: Option[Int], val passed: Long) {
    override def toString = s"$staccato/$volume/$panning/$passed"
  }

  private def startAt(milliseconds: Long, speedUp: Option[Double] = None): String = s"@${milliseconds / speedUp.getOrElse(1d) / 2000}"

  private def instrument(instrument: String): String = s"I[$instrument]"

  private def drum(drum: String): String = s"[$drum]"

  def balance(value: Int): String = s":Controller(8,$value) "

  def volume(value: Int): String = s":Controller(7,$value) "

  // TODO describe speed mapping
  def speed(value: Double): String = s"T${(120 * value).toInt}"

  //  private def speedVolume(settings: Settings): String = s"${speed(settings.speed)} ${volume(settings.volume)}"

  // TODO check why voice 9 doesn't work
  private def metronome(duration: Long, eventSpeed: Option[Double], speed: Double): String = {
    val eventSpeedActual = eventSpeed match {
      // TODO what's meant by event duration speed normal anyway?
      case Some(v) => if (v != 0) v else 1D
      case None => 1D
    }
    logger.debug(s"metronome duration=$duration, eventSpeedActual=$eventSpeedActual, speed=$speed")
    // TODO dirty fix (disable metronome)
    if (eventSpeedActual <= 10D) {
      val every = 1000
      (0 to (duration / speed / every * eventSpeedActual).toInt).
        map(sec => s"${startAt(sec * every)} 70q").mkString(" V10 I[Woodblock] ", " ", " ")
    } else ""
  }

  // TODO check why it doesn't work
  //  private def eitherContent[T, U](either: Either[T, U]) = either.left.getOrElse(either.right.get)

  private def musicEvents(traces: XLog, settings: Settings): Seq[MusicEvent] = t(TSG_musicEvents) {
    logger.debug(s"musicEvents")
    // extraction and what to operate on (event or trace)
    type ExtractorType[T] = (XElement => Option[T], MappingCategoryApplication)
    type OptionalExtractorType[T] = Option[ExtractorType[T]]

    var instrumentDrumExtractor: ExtractorType[String] = null
    var melodyRhythmExtractor: ExtractorType[String] = null
    var volumeExtractor: OptionalExtractorType[String] = None
    var panningExtractor: OptionalExtractorType[String] = None

    var instrumentDrumMapping: Either[InstrumentMapping, DrumMapping] = null
    var melodyRhythmMapping: Either[MelodyMapping, RhythmMapping] = null
    var volumeMapping: Option[VolumeMapping] = None
    var panningMapping: Option[PanningMapping] = None

    val x: XElement => Option[Any] = a => null

    for {
      (k, v) <- settings.mapping
      sonification <- v.sonification
    } {
      // TODO improve
      val extractor = if (k.isEvent) k.asInstanceOf[MappingCategory[String, XElement]].extractor
      else k.asInstanceOf[MappingCategory[String, XElement]].extractor
      val application = k.application
      sonification match {
        case i: InstrumentMapping =>
          instrumentDrumExtractor = (extractor, application)
          instrumentDrumMapping = Left(i)
        case m: MelodyMapping =>
          melodyRhythmExtractor = (extractor, application)
          melodyRhythmMapping = Left(m)
        case d: DrumMapping =>
          instrumentDrumExtractor = (extractor, application)
          instrumentDrumMapping = Right(d)
        case r: RhythmMapping =>
          melodyRhythmExtractor = (extractor, application)
          melodyRhythmMapping = Right(r)
        case v: VolumeMapping =>
          volumeExtractor = Some((extractor, application))
          volumeMapping = Some(v)
        case p: PanningMapping =>
          panningExtractor = Some((extractor, application))
          panningMapping = Some(p)
      }
    }

    val firstTime = traces.firstStartLastEndTotalDuration._1.getTime

    for {
      trace <- traces
      event <- trace
      time <- OpenXESHelper.timeTransitionAttributeValue(event)
      passed = time.getTime() - firstTime if (passed >= 0)
      elementInstrument = if (instrumentDrumExtractor._2 == Event) event else trace
      // make sure instrument and melody are enabled for this event
      instrumentDrumMappingActual = (instrumentDrumMapping).left.getOrElse(instrumentDrumMapping.right.get)
      instrumentDrumEventValue <- instrumentDrumExtractor._1(elementInstrument) if instrumentDrumMappingActual.values(instrumentDrumEventValue).enabled
      elementMelody = if (melodyRhythmExtractor._2 == Event) event else trace
      melodyRhythmMappingActual = melodyRhythmMapping.left.getOrElse(melodyRhythmMapping.right.get)
      melodyRhythmEventValue <- melodyRhythmExtractor._1(elementMelody) if melodyRhythmMappingActual.values(melodyRhythmEventValue).enabled
    } yield {
      // TODO check get
      val volume = for {
        extractor <- volumeExtractor
        mapping <- volumeMapping
        value <- extractor._1(event).asInstanceOf[Option[Double]]
        valueMapped <- mapping.values.get(value)
      } yield valueMapped
      val panning = for {
        extractor <- panningExtractor
        mapping <- panningMapping
        value <- extractor._1(event).asInstanceOf[Option[Double]]
        valueMapped <- mapping.values.get(value)
      } yield valueMapped
      val instrumentDrumMappingValue = instrumentDrumMappingActual.values(instrumentDrumEventValue).value
      val melodyRhythm = melodyRhythmMappingActual.values(melodyRhythmEventValue).value
      val staccato = instrumentDrumMapping match {
        case Left(_) => Left(s"${instrument(instrumentDrumMappingValue)} $melodyRhythm")
        case Right(_) =>
          val drumValue = drum(instrumentDrumMappingValue)
          val drumRhythmStaccato = melodyRhythm.split(" ").map(e => s"$drumValue$e").mkString(" ")
          Right(drumRhythmStaccato)
      }
      new MusicEvent(staccato, volume, panning, passed)
    }
  }

  private def staccato(events: Seq[MusicEvent], globalVolume: Int, volumeMapped: Boolean, panningMapped: Boolean, speedUp: Option[Double]): String = t(TSG_staccato) {
    // TODO currently only round robin way of putting events on different voices
    // Voice 9 is reserved for the metronome
    def iterator(data: Array[String]) = new Iterator[String] {
      private var i = -1
      val hasNext = true
      def next(): String = {
        i += 1
        data(i % data.length)
      }
    }
    val voicesUsed = ((0 to 8) ++ (10 to 15)).toArray.map(e => s"V$e ")
    val nextVoice = iterator(voicesUsed)

    val voiceAndlayersUsed = (0 to 15).toArray.map(e => s"L$e ")
    val nextVoiceLayer = iterator(voiceAndlayersUsed)

    // if the global volume is not set to maximum all events have to adapted accordingly
    val volumeFactor = globalVolume / 127D
    val instrument = events.head.staccato.isLeft
    println(s"instrument=$instrument")
    // determine voice (instrument) or voice and layer (drums)
    events.sortBy(_.passed).map { e =>
      val eventVolume = if (volumeMapped) {
        val volumeNumeric = Math.round(e.volume.getOrElse(127) * volumeFactor)
        volume(volumeNumeric.toInt)
      } else { "" }
      val eventPanning = if (panningMapped) balance(e.panning.getOrElse(127)) else ""
      val voiceWithOptionalLayer = if (instrument) nextVoice.next else nextVoiceLayer.next
      val staccatoValue = e.staccato.left.getOrElse(e.staccato.right.get)

      s"$voiceWithOptionalLayer$eventVolume$eventPanning${startAt(e.passed, speedUp)} $staccatoValue"
    }.mkString(" ")
  }

  def makeStaccato(log: XLog, settings: Settings): String = t(TSG_makeStaccato) {
    val volumeMapped = settings.mapping.values.exists(_.sonification.exists(_.typ == VolumeType))
    val panningMapped = settings.mapping.values.exists(_.sonification.exists(_.typ == PanningType))
    val metronomStaccato = if (settings.metronome) {
      val eventSpeed = settings.eventDurationSpeed orElse settings.eventSpeed
      metronome(log.firstStartLastEndTotalDuration._3, eventSpeed, settings.speed)
    } else ""
    val events = musicEvents(log, settings)
    val eventSpeed = settings.eventDurationSpeed.map { e =>
      println(s"eventDurationSpeed $e speed=${settings.speed}")
      if (e == 0) Some(1d) else Some(e)
    } orElse settings.eventSpeed.map { e =>
      Some(e)
    } getOrElse None
    val speedStaccato = eventSpeed map { e => speed(e) } getOrElse {
      if (settings.speed != DefaultSpeed.speedfactor) speed(settings.speed) else ""
    }
    val speedUp = eventSpeed.map { e => settings.speed / e }
    val instrumentMelody = events.head.staccato.isLeft
    val eventsStaccato = staccato(events, settings.volume, volumeMapped, panningMapped, speedUp)
    val volumeStaccato = if (!volumeMapped) volume(settings.volume) else ""
    val fixedVoice = if (instrumentMelody) "" else "V9"
    s"$speedStaccato $volumeStaccato $metronomStaccato $fixedVoice $eventsStaccato"
  }

}