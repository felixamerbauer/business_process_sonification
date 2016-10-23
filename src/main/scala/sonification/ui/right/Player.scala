package sonification.ui.right

import java.awt.BorderLayout
import java.awt.event.{ ActionEvent, ActionListener }
import java.io.File
import scala.concurrent.duration.{ DurationInt, FiniteDuration }
import org.apache.commons.io.FilenameUtils.{ getBaseName, getExtension }
import org.processmining.framework.util.ui.widgets.BorderPanel
import com.typesafe.scalalogging.StrictLogging
import javax.swing.{ BoxLayout, ImageIcon, JButton, JCheckBox, JFileChooser, JPanel }
import javax.swing.filechooser.FileNameExtensionFilter
import sonification.controller.{ Controller, Modifiable, code2actionListener }
import javax.swing.JComboBox

case class SpeedfactorSliderLabel(speedfactor: Double, slider: Int, label: String)

case class DurationSliderLabel(duration: FiniteDuration, slider: Int, label: String)

object Player {
  val Speeds = Seq(
    (0, "1/4x", 0.25),
    (10, "1/2x", 0.5),
    (20, "1x", 1d),
    (30, "2x", 2d),
    (40, "4x", 4d),
    (50, "6x", 6d),
    (60, "8x", 8d),
    (70, "10x", 10d),
    (80, "20x", 20d),
    (90, "50x", 50d),
    (100, "100x", 100d),
    (110, "200x", 200d),
    (120, "500x", 500d),
    (130, "1kx", 1000d),
    (140, "10kx", 10000d)).map {
      case (slider, label, speedfactor) =>
        SpeedfactorSliderLabel(speedfactor, slider, label)
    }
  val DefaultSpeed = Speeds.find(_.speedfactor == 1).get
  val DurationEventSpeeds = Seq(
    (0, "1/4x", 0.25),
    (10, "1/2x", 0.5),
    (20, "1x", 1d),
    (30, "2x", 2d),
    (40, "5x", 5d),
    (50, "10x", 10d),
    (60, "20x", 20d),
    (70, "50x", 50d),
    (80, "100x", 100d),
    (90, "Normal", 0d)).map {
      case (slider, label, speedfactor) =>
        SpeedfactorSliderLabel(speedfactor, slider, label)
    }
  val DefaultDurationEventSpeed = DurationEventSpeeds.find(_.speedfactor == 0).get

  val Durations = Seq(
    (0, "10s", 10 seconds),
    (10, "20s", 20 seconds),
    (20, "30s", 30 seconds),
    (30, "1m", 1 minute),
    (40, "2m", 2 minutes),
    (50, "5m", 5 minutes),
    (60, "10m", 10 minutes),
    (70, "30m", 30 minutes),
    (80, "1h", 1 hour)).map {
      case (slider, label, duration) =>
        DurationSliderLabel(duration, slider, label)
    }

  val DefaultDuration = Durations.find(_.duration == 1.minute).get

}

class Player(implicit ctrl: Controller) extends BorderPanel(2, 2) with Modifiable with StrictLogging {
  // TODO doesn't resize when all elements fit in one row
  setLayout(new BoxLayout(this, BoxLayout.Y_AXIS))
  //  setPreferredSize(new Dimension(1000, 500))
  //  println("default layout " + getLayout)
  private val player = this

  val top = new JPanel
  // TODO SlickerFactory button doesn't use icon
  val pause = new JButton
  val play = new JButton
  val stop = new JButton
  val downloadMidi = new JButton
  val downloadWave = new JButton
  val metronom = new JCheckBox
  // Icons used for buttons
  def icon(file: String) = new ImageIcon(getClass().getResource(s"/$file"))

  pause.setIcon(icon("pause.png"))
  pause.setToolTipText("Pause")
  pause.addActionListener { ctrl.pause }
  play.setIcon(icon("play.png"))
  play.setToolTipText("Play")
  play.addActionListener { ctrl.play }
  stop.setIcon(icon("stop.png"))
  stop.setToolTipText("Stop")
  stop.addActionListener { ctrl.stop }
  downloadMidi.setIcon(icon("midi.png"))
  downloadMidi.setToolTipText("""Download Midi""")
  downloadWave.setIcon(icon("wave.png"))
  downloadWave.setToolTipText("""Download Audio (Wave)""")
  metronom.setText("Metronom")
  metronom.addActionListener { ctrl.metronom(metronom.isSelected) }

  top.add(pause)
  top.add(play)
  top.add(stop)
  top.add(downloadMidi)
  top.add(downloadWave)
  top.add(metronom)

  // use the desktop as default save location if available
  private val windowsDesktop = Option(System.getProperty("user.home")).map(new File(_, "Desktop")).filter(_.isDirectory())

  // if the application is running in standalone mode 
  val openXes = new JButton
  val loadProject = new JButton
  val saveProject = new JButton
  ctrl.parentFrame.foreach { parentFrame =>
    // open Xes
    openXes.setText("XES")
    openXes.setToolTipText("""Open XES file""")
    openXes.addActionListener(new ActionListener() {
      val fc = new JFileChooser()
      // http://stackoverflow.com/questions/24309517/custom-jfilechooser-how-to-add-jcombobox-into-the-jfilechooser
      val combo = new JComboBox(Array("Normal", "Sequential"))
      val panel1 = fc.getComponent(3).asInstanceOf[JPanel]
      val panel2 = panel1.getComponent(3).asInstanceOf[JPanel]
      panel2.add(combo)
      val xesFilter = new FileNameExtensionFilter("XES files (*.xes)", "xes")
      fc.addChoosableFileFilter(xesFilter)
      fc.setFileFilter(xesFilter)
      fc.setDialogTitle("Open XES file")
      windowsDesktop.foreach(fc.setCurrentDirectory)

      def actionPerformed(e: ActionEvent) {
        //Handle open button action.
        val returnVal = fc.showOpenDialog(player)
        if (returnVal == JFileChooser.APPROVE_OPTION) {
          val file = fc.getSelectedFile()
          //This is where a real application would open the file.
          val sequential = combo.getSelectedIndex == 1
          ctrl.loadXES(file, sequential)
        } else {
          logger.info("Open command cancelled by user.")
        }
      }
    })
    top.add(openXes)

    // load project
    //    loadProject.setIcon(icon("open.png"))
    loadProject.setText("load")
    loadProject.setToolTipText("""Load project""")
    loadProject.addActionListener(new ActionListener() {
      val fc = new JFileChooser()
      val sonFilter = new FileNameExtensionFilter("Sonification project files (*.son)", "son")
      fc.addChoosableFileFilter(sonFilter)
      fc.setFileFilter(sonFilter)
      fc.setDialogTitle("Open sonification project")
      windowsDesktop.foreach(fc.setCurrentDirectory)

      def actionPerformed(e: ActionEvent) {
        //Handle open button action.
        val returnVal = fc.showOpenDialog(player)
        if (returnVal == JFileChooser.APPROVE_OPTION) {
          val file = fc.getSelectedFile()
          //This is where a real application would open the file.
          logger.info("Opening: " + file.getName())
          ctrl.loadProject(file)
        } else {
          logger.info("Open command cancelled by user.")
        }
      }
    })
    top.add(loadProject)

    // save project
    //    saveProject.setIcon(icon("open.png"))
    saveProject.setText("save")
    saveProject.setToolTipText("""Save project""")
    saveProject.addActionListener(new ActionListener() {
      // let user choose midi file destination
      val fc = new JFileChooser()
      val sonFilter = new FileNameExtensionFilter("Sonification project files (*.son)", "son")
      fc.addChoosableFileFilter(sonFilter)
      fc.setFileFilter(sonFilter)
      fc.setDialogTitle("Save sonification project file")
      windowsDesktop.foreach(fc.setCurrentDirectory)
      def actionPerformed(e: ActionEvent) {
        val returnVal = fc.showSaveDialog(player)
        if (returnVal == JFileChooser.APPROVE_OPTION) {
          val file = checkFileName(fc.getSelectedFile, "son")
          logger.info(s"Saving project: ${file.getCanonicalPath()}")
          ctrl.saveProject(file)
        } else {
          logger.debug("Save command cancelled by user.")
        }
      }
    })
    top.add(saveProject)
  }
  val volume = new VolumePanel(this)
  top.add(volume)
  add(top, BorderLayout.CENTER)

  private def checkFileName(file: File, extension: String): File =
    if (getExtension(file.getName()).equalsIgnoreCase(extension)) file
    // remove the extension (if any) and replace it with ".xyz"
    else new File(file.getParentFile(), s"${getBaseName(file.getName())}.$extension")

  // handle click on download midi button
  downloadMidi.addActionListener(new ActionListener() {
    // let user choose midi file destination
    val fc = new JFileChooser()
    val midiFilter = new FileNameExtensionFilter("MIDI files (*.midi)", "midi")
    fc.addChoosableFileFilter(midiFilter)
    fc.setFileFilter(midiFilter)
    fc.setDialogTitle("Save MIDI file")
    windowsDesktop.foreach(fc.setCurrentDirectory)
    def actionPerformed(e: ActionEvent) {
      val returnVal = fc.showSaveDialog(player)
      if (returnVal == JFileChooser.APPROVE_OPTION) {
        val file = checkFileName(fc.getSelectedFile, "midi")
        logger.info(s"Saving Midi: ${file.getCanonicalPath()}")
        ctrl.saveMidi(file)
      } else {
        logger.debug("Save command cancelled by user.")
      }
    }
  })

  // handle click on download wave button
  downloadWave.addActionListener(new ActionListener() {
    // let user choose midi file destination
    val fc = new JFileChooser()
    val wavFilter = new FileNameExtensionFilter("wave files (*.wav)", "wav")
    fc.addChoosableFileFilter(wavFilter)
    fc.setFileFilter(wavFilter)
    fc.setDialogTitle("Save wave file")
    windowsDesktop.foreach(fc.setCurrentDirectory)

    def actionPerformed(e: ActionEvent) {
      val returnVal = fc.showSaveDialog(player)
      if (returnVal == JFileChooser.APPROVE_OPTION) {
        val file = checkFileName(fc.getSelectedFile, "wav")
        logger.info(s"Saving audio: ${file.getCanonicalPath()}")
        ctrl.saveAudio(file)
      } else {
        logger.debug("Save command cancelled by user.")
      }
    }
  })

  val speed = new SpeedPanel(this)
  add(speed, BorderLayout.SOUTH)
}

