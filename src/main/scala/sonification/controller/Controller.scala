package sonification.controller

import java.awt.event.{ ActionEvent, ActionListener }
import java.io.{ File, FileOutputStream }
import java.nio.file.Files
import scala.Option.option2Iterable
import scala.collection.JavaConversions.asScalaBuffer
import scala.concurrent.ExecutionContext.Implicits
import scala.concurrent.duration.FiniteDuration
import scala.util.Try
import org.deckfour.xes.model.XLog
import org.jfugue.player.ManagedPlayerListener
import com.typesafe.scalalogging.StrictLogging
import javax.sound.midi.Sequence
import javax.swing.{ JComponent, JOptionPane, KeyStroke, SwingUtilities, Timer }
import sonification.StandaloneFrame
import sonification.controller.Update.{ UDuration, UFilter, UGlobalMapping, UJumpTo, ULog, UMetronom, USonificationMapping, USpeed, UTraces, UVisualizationMapping, UVolume, UZoom, Update }
import sonification.music.{ Midi2WavRenderS, MusicCommons }
import sonification.music.MusicCommons.Microsecond
import sonification.music.PlayerWrapper
import sonification.music.StaccatoGenerator.makeStaccato
import sonification.music.RhythmStaccatoValidationCache
import sonification.openxes.MyXesParser
import sonification.openxes.OpenXESHelper.{ RichLog, RichTrace }
import sonification.ui.{ SwingHelper, View }
import sonification.ui.left.{ Visualization, VisualizationOverview }
import sonification.ui.right.Player
import org.deckfour.xes.model.XElement
import sonification.music.MelodyStaccatoValidationCache
import java.nio.file.Paths
import java.util.concurrent.TimeUnit

/**
 * Main controller
 * @param log (immutable) model
 * @param parentFrame links to parent frame (only set if run in standalone mode)
 */
class Controller(val log: XLog, initialSettings: Settings, val parentFrame: Option[StandaloneFrame]) extends ActionListener with ManagedPlayerListener with StrictLogging {
  // threadpool config for playing jfuge music string
  import scala.concurrent.ExecutionContext.Implicits.global

  def this(log: XLog, settings: Settings) { this(log, settings, None) }

  logger.info(s"Controller running from ${Paths.get("").toAbsolutePath()}")

  // current state of controller (start in state Stop)
  var state: State = Stop
  var staccato: Option[String] = None
  var sequence: Option[Sequence] = None
  var sequenceDuration: Option[Microsecond] = None
  // handles the playback for the main player
  val pw = new PlayerWrapper()

  // link to view
  var view: View = null
  def setView(view: View) {
    this.view = view
    // register keyboard events
    val keyForward = "keyForward"
    val keyBackward = "keyBackward"
    val keyClipboard = "keyClipboard"
    val inputMap = view.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
    inputMap.put(KeyStroke.getKeyStroke('y'), keyForward)
    inputMap.put(KeyStroke.getKeyStroke('x'), keyBackward)
    inputMap.put(KeyStroke.getKeyStroke('c'), keyClipboard)
    shortcutActionForward = new ShortcutAction(-10, this)
    shortcutActionBackward = new ShortcutAction(10, this)
    view.getActionMap.put(keyForward, shortcutActionForward)
    view.getActionMap.put(keyBackward, shortcutActionBackward)
    view.getActionMap.put(keyClipboard, new CopySettingsToClipboardAction(this))
  }

  var shortcutActionForward: ShortcutAction = null
  var shortcutActionBackward: ShortcutAction = null

  // currently selected traces
  var traces: RichLog = log
  // initialize settings
  var settings = initialSettings

  private var timer = new Timer(1000 / 30, this)
  
  // load sound bank 
  // TODO put on worker thread
  MusicCommons.changeSoundbank

  // initalize view
  def initView() {
    logger.debug("initView")
    //    SwingUtilities.invokeLater(new Runnable() {
    //          override def run() {

    val right = view.MainSplitPane.right
    // remove existing global and category settings
    right.globalMapping.removeAll()
    right.resetTabs()
    right.addBasicTabs()

    right.globalMapping.update()
    right.addMappings()

    right.player.stop.setEnabled(false)
    right.player.pause.setEnabled(false)
    right.player.volume.slider.setValue(settings.volume)
    settings.duration match {
      // duration defined
      case Some(duration) =>
        right.player.speed.durationSlider.setValue(Player.Durations.find(_.duration == duration).getOrElse {
          logger.warn(s"Note found duration $duration")
          Player.DefaultDuration
        }.slider)
        // set the event duration if required
        settings.eventDurationSpeed foreach { eventDurationSpeed =>
          right.player.speed.eventDurationSlider.setValue(Player.DurationEventSpeeds.find(_.speedfactor == eventDurationSpeed).getOrElse {
            logger.warn(s"Not found eventDurationSpeed $eventDurationSpeed")
            Player.DefaultDurationEventSpeed
          }.slider)
        }
        // update the UI to duration mode
        right.player.speed.enabaleDuration()

      // speed defined
      case None =>
        right.player.speed.speedSlider.setValue(Player.Speeds.find(_.speedfactor == settings.speed).getOrElse {
          logger.warn(s"Not found speed ${settings.speed}")
          Player.DefaultSpeed
        }.slider)
        settings.eventSpeed foreach { eventSpeed =>
          right.player.speed.eventSpeedSlider.setValue(Player.Speeds.find(_.speedfactor == eventSpeed).getOrElse {
            logger.warn(s"Not found eventSpeed $eventSpeed")
            Player.DefaultSpeed
          }.slider)
        }
    }
    right.player.metronom.setSelected(settings.metronome)
    // if log is longer than one hour disable metronom
    val (_, _, duration) = log.firstStartLastEndTotalDuration
    if (duration >= 60 * 60 * 1000) {
      logger.debug("Disabling metronom as log is too long")
      right.player.metronom.setEnabled(false)
    }

    updateStartEndDurations()
    for ((trace, idx) <- log.zipWithIndex) {
      val (start, end, duration) = trace.startEndDuration
      val startEnd = s"${dtf.format(start)} to ${dtf.format(end)}"
      val name = trace.name.getOrElse("")
      val desc = s"""Trace ${idx + 1} ($name) from $startEnd (${calculatePositionString(duration)}) with ${trace.size} events"""
      right.tracesSelection.add(idx, desc, settings.traces.contains(idx))
    }

  }

  private def updateStartEndDurations() {
    val text = if (traces.log.isEmpty) ""
    else {
      val (firstStart, lastEnd, totalDuration) = traces.firstStartLastEndTotalDuration
      val size = MusicCommons.fileSize(totalDuration)
      s"${dtf.format(firstStart)} - ${dtf.format(lastEnd)} (${calculatePositionString(totalDuration, Some(settings.speed))}) / $size"
    }
    view.MainSplitPane.left.zoomPositionDuration.totalDuration.setText(text)
  }

  // calculate the pixels for moving the progress bar
  var fraction: Float = 0

  // timer event
  override def actionPerformed(e: ActionEvent) {
    // progress of the PlayerWrapper is at 100% after the last event is played 
    // but we are interested when the last event starts to play 
    fraction = {
      val traceDuration = traces.firstStartLastEndTotalDuration._3
      val sequenceMs = sequenceDuration.get.toMs
      val diff = sequenceMs - traceDuration

      val f = Math.min(pw.progress * sequenceMs / traceDuration * settings.speed, 1)
      //      logger.debug(s"traceDuration=$traceDuration, sequenceMs=$sequenceMs, progress=${PlayerWrapper.progress}, f=$f")
      f.toFloat
    }
    val passed = fraction * traces.firstStartLastEndTotalDuration._3
    view.MainSplitPane.left.zoomPositionDuration.currentPosition.setText(calculatePositionString((passed * settings.speed).toInt, None))
    view.MainSplitPane.left.visualizationOverview.repaint()
    view.MainSplitPane.left.visualization.repaint()
  }

  // only standalone
  def loadXES(file: File, sequential: Boolean) {
    logger.info(s"Opening: ${file.getName} sequential=$sequential")
    SwingHelper.doBackgroundWorkWithWaitCursor(view, MyXesParser.parse(file)) match {
      case Some(log) =>
        val adjustedLog = if (sequential) log.cloneSequential else log
        val initialSetttings = SwingHelper.doBackgroundWorkWithWaitCursor(view, SettingsUtil.defaultSettings(adjustedLog))
        val root = SwingUtilities.getRoot(view).asInstanceOf[StandaloneFrame]
        root.initialize(adjustedLog, initialSetttings, s"${file.getName}${if (sequential) " seqential" else ""}")
      case None => JOptionPane.showMessageDialog(null, s"XES file couldn't be read and parse. Please select another file.");
    }
  }

  def loadProject(file: File) {
    logger.debug(s"loadProject $file")
    SwingHelper.doBackgroundWorkWithWaitCursor(view, {
      val bytes = Files.readAllBytes(file.toPath)
      val project = Project.deserialize(bytes)
      project foreach { e =>
        logger.debug(s"loaded project from file $file\n$e")
        val root = SwingUtilities.getRoot(view).asInstanceOf[StandaloneFrame]
        // always load with displaying all traces on one page
        root.initialize(e.log, e.settings.copy(zoomLevel = FitPage), file.getName)
      }
    })
  }

  def saveProject(file: File) {
    logger.debug(s"saveProject $file")
    SwingHelper.doBackgroundWorkWithWaitCursor(view, {
      val project = Project(log, settings)
      val bytes = project.serialize
      val fos = new FileOutputStream(file)
      fos.write(bytes)
      fos.close()
    })
  }
  // click handlers
  def updatedSettings(updates: Update*) {

    // check if sonification can be played
    def checkSonification() {
      val checkInstrumentsAndMelodyUsed: Boolean = settings.mapping.values.flatMap(_.sonification).filter(_.mandatory).size == 2

      def checkStaccatos: Boolean = {
        val allStaccatos = settings.mapping.values.map(_.allStaccatos).foldLeft(Staccatos.empty) { (a, b) =>
          Staccatos(a.melodies ++ b.melodies, a.rhythms ++ b.rhythms)
        }
        logger.debug(s"Music strings to check\n\t$allStaccatos")
        !allStaccatos.isEmpty && allStaccatos.melodies.forall(MelodyStaccatoValidationCache.valid) && allStaccatos.rhythms.forall(RhythmStaccatoValidationCache.valid)
      }
      println(s"checkInstrumentsAndMelodyUsed $checkInstrumentsAndMelodyUsed checkStaccatos $checkStaccatos")
      settings.sonificationOk = !settings.traces.isEmpty && checkInstrumentsAndMelodyUsed && checkStaccatos
      switchPlaybackRelatedControls(settings.sonificationOk)
    }
    // check if visualization can be drawn
    def checkVisualization() {
      switchVisualizationRelatedControls(!settings.traces.isEmpty)
    }
    // helper
    //  ULog, UTraces, UFilter, 
    //  USonificationMapping, UVisualizationMapping, UGlobalMapping,
    //  UVolume, UBalance, USpeed, UDuration, UMetronom,
    //  UZoom
    // determine which updates are necessary
    case class Effect(name: String, effect: () => Unit, triggers: Update*) {
      private val intersect = triggers intersect updates
      val toRun = !intersect.isEmpty
      override def toString = s"$name: ${intersect.mkString(", ")} -> $toRun}"
    }

    def repaint() {
      Visualization.dirty = true
      VisualizationOverview.dirty = true
      view.MainSplitPane.left.repaint()
      updateStartEndDurations
    }
    def sonificationToNone() {
      staccato = None
      sequence = None
      sequenceDuration = None
    }
    val effects = Seq(
      Effect("check sonification", checkSonification,
        UFilter, UTraces, ULog, UGlobalMapping),
      Effect("check visualization", checkVisualization,
        UFilter, UTraces, ULog, UGlobalMapping),
      // TODO USonificationMapping should only trigger 
      Effect("repainting necessary", repaint,
        UFilter, UTraces, ULog, UGlobalMapping, UVisualizationMapping, USonificationMapping, UZoom, UJumpTo, USpeed, UDuration),
      Effect("update sonification", sonificationToNone,
        UFilter, UTraces, ULog, UGlobalMapping, USonificationMapping, UVolume, USpeed, UDuration, UMetronom),
      Effect("udpate XML", view.MainSplitPane.right.xmlTab.update,
        UFilter, UTraces, ULog),
      Effect("udpate Parallelism", view.MainSplitPane.right.parallelismTab.update,
        UFilter, UTraces, ULog),
      Effect("init view", initView,
        UGlobalMapping))

    logger.debug(s"updatedSettings ${updates.mkString(", ")} -> ${effects.filter(_.toRun).mkString(" | ")}")
    // run all relevant effects
    effects.filter(_.toRun).foreach(_.effect())

    logger.debug(s"updated settings: $settings")
  }

  /**
   * TODO comment
   * @param category
   * @param sonification
   */
  def globalSonificationMapping(category: MappingCategory[_, _ <: XElement], sonification: Option[SonificationType]) {
    logger.debug(s"globalSonificationMapping category=$category, sonification=$sonification")
    val updatedMapping = SettingsUtil.updateSonification(category, sonification, log, settings.mapping)
    settings = settings.copy(mapping = updatedMapping)
    updatedSettings(UGlobalMapping)
  }

  def literalMapping(sonification: Option[LiteralMapping[_]], visualization: Option[LiteralMapping[_]], key: String, enabled: Boolean) {
    logger.debug(s"literalMapping $sonification, $visualization, $key, $enabled")
    SettingsUtil.literalMapping(sonification, visualization, key, enabled)
    updatedSettings(UFilter)
  }

  def literalMapping(mapping: LiteralMapping[_], key: String, value: Any) {
    logger.debug(s" literalMapping mapping=$mapping, key=$key, value=$value")
    SettingsUtil.literalMapping(mapping, key, value)
    updatedSettings(USonificationMapping)
  }

  def traceSelection(idx: Int, selected: Boolean) {
    logger.debug(s"traceSelection $idx $selected")
    settings =
      if (selected) settings.copy(traces = settings.traces + idx)
      else settings.copy(traces = settings.traces - idx)
    // clone the original log and use the active traces
    traces = log.selectTraces(settings.traces)
    updatedSettings(UTraces)
  }

  def tracesSelectionNone() {
    logger.debug("tracesSelectionNone")
    settings = settings.copy(traces = Set())
    // clone the original log and remove all traces
    traces = log.selectTraces(settings.traces)
    view.MainSplitPane.right.tracesSelection.toggle(false)
    updatedSettings(UTraces)
  }

  def tracesSelectionAll() {
    logger.debug("traceSelectionsAll")
    settings = settings.copy(traces = Set(0 until log.size: _*))
    // just use the original log
    traces = log
    view.MainSplitPane.right.tracesSelection.toggle(true)
    updatedSettings(UTraces)
  }

  def prelistenInstrument(instrument: String) {
    logger.debug(s"prelistenInstrument $instrument")
    MusicCommons.instrumentSample(instrument)
  }

  def prelistenDrum(drum: String) {
    logger.debug(s"prelistenDrum $drum")
    MusicCommons.drumSample(drum)
  }

  def prelistenMelody(melody: String, speed: Double) {
    logger.debug(s"prelistenMelody $melody $speed")
    MusicCommons.melodySample(melody, speed)
  }

  def prelistenRhythm(rhythm: String, speed: Double) {
    logger.debug(s"prelistenRhythm $rhythm $speed")
    MusicCommons.rhythmSample(rhythm, speed)
  }

  /**
   * metronom changed
   * @param metronom new metronom
   */
  def metronom(metronome: Boolean) {
    logger.debug(s"setting metronom to $metronome")
    settings = settings.copy(metronome = metronome)
    updatedSettings(UMetronom)
  }

  /**
   * metronom changed
   * @param metronom new metronom
   */
  def zoomLevel(zoomLevel: ZoomLevel) {
    logger.debug(s"setting zoomLevel to $zoomLevel")
    settings = settings.copy(zoomLevel = zoomLevel)
    updatedSettings(UZoom)
  }

  /**
   * volume changed
   * @param volume new volume
   */
  def volume(volume: Int) {
    logger.debug(s"setting volume to $volume")
    settings = settings.copy(volume = volume)
    if (volume > 0) {
      MusicCommons.playVolumeSound(volume)
    }
    updatedSettings(UVolume)
  }

  def switchSpeedDuration() {
    val player = view.MainSplitPane.right.player
    // duration -> speed
    if (settings.duration.isDefined) {
      speed(Player.DefaultSpeed.speedfactor)
      player.speed.speedSlider.setValue(Player.DefaultSpeed.slider)
      player.speed.eventSpeedSlider.setValue(Player.DefaultSpeed.slider)
    } // speed -> duration
    else {
      duration(Player.DefaultDuration.duration, Some(Player.DefaultDurationEventSpeed.speedfactor))
      player.speed.durationSlider.setValue(Player.DefaultDuration.slider)
      player.speed.eventDurationSlider.setValue(Player.DefaultDurationEventSpeed.slider)
    }
    player.speed.checkbox.setSelected(true)
    updatedSettings(USpeed)
  }

  def speed(v: Double) {
    logger.debug(s"setting speed to $v")
    val player = view.MainSplitPane.right.player
    settings = settings.copy(speed = v, duration = None, eventDurationSpeed = None)
    // move the event speeder slider along the speed slider if not enabled
    if (settings.eventSpeed.isEmpty) {
      player.speed.eventSpeedSlider.setValue(player.speed.speedSlider.getValue)
    }
    updatedSettings(USpeed)
  }

  /**
   * speed changed
   * @param speed new speed
   */
  def speed(v: Int) { speed(Player.Speeds.find(_.slider == v).get.speedfactor) }

  def eventSpeed(v: Double) {
    logger.debug(s"setting eventSpeed to $v")
    settings = settings.copy(eventSpeed = Some(v), duration = None)
    updatedSettings(USpeed)
  }

  def eventSpeed(v: Int) {
    eventSpeed(Player.Speeds.find(_.slider == v).get.speedfactor)
  }

  def eventOrEventDurationSpeed(enabled: Boolean) {
    val speed = view.MainSplitPane.right.player.speed
    val eventSpeed = if (settings.duration.isDefined) {
      val eventDurationSpeed = if (enabled) Some(1d) else None
      settings = settings.copy(eventDurationSpeed = eventDurationSpeed)
      speed.eventDurationSlider.setEnabled(enabled)
      speed.eventDurationSlider.setValue(Player.DefaultDurationEventSpeed.slider)
    } else {
      val eventSpeed = if (enabled) Some(settings.speed) else None
      settings = settings.copy(eventSpeed = eventSpeed)
      speed.eventSpeedSlider.setEnabled(enabled)
      if (!enabled) {
        speed.eventSpeedSlider.setValue(speed.speedSlider.getValue)
      }
    }
    updatedSettings(USpeed)
  }

  def duration(v: FiniteDuration, eventDurationSpeed: Option[Double] = None) {
    logger.debug(s"setting duration to $v")
    // calculate the speed for the desired duration
    val (_, _, logDuration) = traces.firstStartLastEndTotalDuration
    val speed = logDuration / v.toMillis.toDouble
    logger.debug(s"log duration: $logDuration, target duration: $v, speed: $speed")
    settings = settings.copy(speed = speed, eventSpeed = None, duration = Some(v), eventDurationSpeed = eventDurationSpeed.orElse(settings.eventDurationSpeed))
    updatedSettings(UDuration)
  }

  def duration(v: Int) { duration(Player.Durations.find(_.slider == v).get.duration) }

  def eventDurationSpeed(v: Double) {
    logger.debug(s"setting eventDurationSpeed to $v")
    settings = settings.copy(eventDurationSpeed = Some(v))
    updatedSettings(USpeed)
  }

  def eventDurationSpeed(v: Int) {
    eventDurationSpeed(Player.DurationEventSpeeds.find(_.slider == v).get.speedfactor)
  }

  /**
   * enable/disable GUI elements
   * @param allowed
   */
  private def settingsModifiable(oldState: State, newState: State) {
    logger.debug(s"settingsModifiable $oldState -> $newState")
    view.MainSplitPane.right.act(oldState, newState)
    view.MainSplitPane.left.act(oldState, newState)
    view.MainSplitPane.left.visualizationOverview.act(oldState, newState)
  }

  /**
   * Save music string as midi file
   * @param file file name
   * @return result of creating music string, convertig to midi and writing to file
   */
  def saveMidi(file: File) = Try[Unit] {
    calculateSequence()
    def createAndSaveMidi {
      val midi = MusicCommons.getMidi(sequence.get)
      val fos = new FileOutputStream(file)
      fos.write(midi)
      fos.close()
    }
    SwingHelper.doBackgroundWorkWithWaitCursor(view, createAndSaveMidi)
  }

  /**
   * Save music string as wave file
   * @param file file name
   * @return result of creating music string, convertig to wave and writing to file
   */
  def saveAudio(file: File) = Try {
    calculateSequence()
    val os = new FileOutputStream(file)
    SwingHelper.doBackgroundWorkWithWaitCursor(view, Midi2WavRenderS.render(null, sequence.get, os))
    os.close()
  }

  private def switchPlaybackRelatedControls(enable: Boolean) {
    view.MainSplitPane.right.player.play.setEnabled(enable)
    view.MainSplitPane.right.player.downloadMidi.setEnabled(enable)
    view.MainSplitPane.right.player.downloadWave.setEnabled(enable)
  }

  private def switchVisualizationRelatedControls(enable: Boolean) {
    view.MainSplitPane.left.zoomPositionDuration.setEnabled(enable)
    view.MainSplitPane.left.zoom.zoomCombo.setEnabled(enable)
  }

  // handle all required steps when stop occurs (button press, timeout) 
  def stop() {
    logger.info("stop")
    val oldState = state
    // update the state before finishing the player
    state = Stop
    shortcutActionForward.setEnabled(false)
    shortcutActionBackward.setEnabled(false)
    timer.stop()
    pw.finish()
    val right = view.MainSplitPane.right
    right.player.stop.setEnabled(false)
    right.player.pause.setEnabled(false)
    right.player.play.setEnabled(true)
    right.player.volume.slider.setEnabled(true)
    right.player.speed.speedSlider.setEnabled(true)
    right.player.speed.durationSlider.setEnabled(true)
    right.player.speed.eventDurationSlider.setEnabled(true)
    right.player.speed.eventSpeedSlider.setEnabled(true)
    right.player.speed.speedDuration.setEnabled(true)
    right.player.openXes.setEnabled(true)
    right.player.downloadMidi.setEnabled(true)
    right.player.downloadWave.setEnabled(true)
    right.player.metronom.setEnabled(true)
    right.tabs.setEnabled(true)
    right.player.saveProject.setEnabled(true)
    right.player.loadProject.setEnabled(true)

    view.MainSplitPane.left.zoomPositionDuration.setToBeginning()
    fraction = 0
    view.MainSplitPane.left.repaint()
    settingsModifiable(oldState, state)
  }

  // handle all required steps when pause occurs 
  def pause() {
    logger.info("pause")
    val oldState = state
    shortcutActionForward.setEnabled(false)
    shortcutActionBackward.setEnabled(false)
    state = Pause
    view.MainSplitPane.right.player.pause.setEnabled(false)
    view.MainSplitPane.right.player.play.setEnabled(true)
    timer.stop()
    pw.pause
    settingsModifiable(oldState, state)
  }

  // TODO save the option and should work more like a update
  def calculateStaccato() {
    //    println(s"calculateStaccato ${staccato.isDefined}")
    if (staccato.isEmpty) {
      staccato = Some(makeStaccato(traces.log, settings))
    }
  }
  def calculateSequence() {
    // calculate staccato if necessary
    calculateStaccato()
    if (sequence.isEmpty) {
      sequence = Some(MusicCommons.sequence(staccato.get))
    }
    if (sequenceDuration.isEmpty) {
      sequenceDuration = Some(MusicCommons.duration(sequence.get))
    }
  }

  // handle all required steps when play occurs
  def play() {
    logger.info("play")
    val right = view.MainSplitPane.right
    right.player.stop.setEnabled(true)
    right.player.pause.setEnabled(true)
    right.player.play.setEnabled(false)
    right.player.volume.slider.setEnabled(false)
    right.player.speed.speedSlider.setEnabled(false)
    right.player.speed.durationSlider.setEnabled(false)
    right.player.speed.eventDurationSlider.setEnabled(false)
    right.player.speed.eventSpeedSlider.setEnabled(false)
    right.player.speed.speedDuration.setEnabled(false)
    right.player.openXes.setEnabled(false)
    right.player.downloadMidi.setEnabled(false)
    right.player.downloadWave.setEnabled(false)
    right.player.metronom.setEnabled(false)
    right.player.saveProject.setEnabled(false)
    right.player.loadProject.setEnabled(false)
    right.tabs.setEnabled(false)
    state match {
      case Pause => // nothing to do as the sequence was already generated
      case Stop =>
        calculateSequence()
        val traceDuration = traces.firstStartLastEndTotalDuration._3
        val sequenceMs = sequenceDuration.get.toMs
        logger.debug(s"traceDuration=$traceDuration, sequenceMs=$sequenceMs")
        pw.init(sequence.get)
        pw.addListener(this)
        if (fraction > 0) {
          pw.seek(fraction)
        }
      case _ =>
    }
    settingsModifiable(state, Play)
    pw.resume
    timer.start()
    state = Play
    shortcutActionForward.setEnabled(true)
    shortcutActionBackward.setEnabled(true)
  }

  def jumpTo(progress: Double) {
    logger.debug(s"jumpTo $progress")
    fraction = progress.toFloat
    updatedSettings(UJumpTo)
    logger.debug(s"timer jump to $timer")
  }

  def jumpRelative(seconds: Int) {
    logger.debug(s"jumpRelative $seconds")
    sequenceDuration.foreach { e =>
      val currentPosition = (pw.progress * e.toMs).toLong
      // avoid jumping beyond the end...
      val jumpTo = Math.min(currentPosition + seconds * 1000, e.toMilliseconds)
      // ... jumping before the end
      val jumpToProgress = Math.max(jumpTo / e.toMs.toFloat, 0)
      logger.debug(s"duration=${e.toMs}, currentPosition=$currentPosition, jump=$seconds, jumpTo=$jumpTo, jumpToProgress=$jumpToProgress")
      pw.seek(jumpToProgress)
    }
  }

  def visualizationItemSelected(item: VisualizationItem) {
    view.MainSplitPane.right.properties.update(Some(item))
  }

  override def onStarted(seq: Sequence) {
    logger.info(s"onStarted $seq")
  }
  override def onFinished {
    logger.info("onFinished")
    pw.removeListeners()
    if (state != Stop) {
      logger.info("Completed playing audio due to end audio -> stopping")
      stop()
    }
  }
  override def onPaused {
    logger.info("onPaused")
  }
  override def onResumed {
    logger.info("onResumed")
  }
  override def onSeek(tick: Long) {
    logger.info(s"onSeek $tick")
  }

  override def onReset {
    logger.debug("onReset")
  }

}
