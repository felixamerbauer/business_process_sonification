package sonification.ui.right

import java.awt.{ Component, Dimension, Graphics, Graphics2D, GridBagConstraints, GridBagLayout, Insets }
import java.awt.event.{ ActionEvent, ActionListener }

import org.processmining.framework.util.ui.widgets.BorderPanel

import com.typesafe.scalalogging.StrictLogging

import javax.swing.{ BoxLayout, JScrollPane, JTable, JTextArea }
import sonification.controller.{ Controller, Modifiable, VisualizationItem }
import sonification.music.MusicCommons
import sonification.music.MusicCommons.{ staccato, staccatoDrumRhythm }
import sonification.music.StaccatoGenerator
import sonification.openxes.OpenXESHelper.{ PrettyPrinter60wide2indent, serialize }
import sonification.ui.left.Visualization
import sonification.ui.left.VisusalizationCalculator.ItemSizeMain
import sonification.ui.left.XY
import sonification.ui.sf

class Properties(implicit ctrl: Controller) extends BorderPanel(2, 2) with Modifiable with StrictLogging {
  logger.debug("Properties")
  setLayout(new GridBagLayout)

  val gbcIcon = new GridBagConstraints
  gbcIcon.gridx = 0
  gbcIcon.gridy = 0
  gbcIcon.insets = new Insets(2, 2, 2, 1)
  gbcIcon.weightx = 0.04
  gbcIcon.weighty = 1.0
  gbcIcon.fill = GridBagConstraints.BOTH

  val gbcSonificationVisualization = new GridBagConstraints()
  gbcSonificationVisualization.gridx = 1
  gbcSonificationVisualization.gridy = 0
  gbcSonificationVisualization.insets = new Insets(2, 1, 2, 1)
  gbcSonificationVisualization.weightx = 0.32
  gbcSonificationVisualization.weighty = 1.0
  gbcSonificationVisualization.fill = GridBagConstraints.BOTH

  val gbcEvent = new GridBagConstraints()
  gbcEvent.gridx = 3
  gbcEvent.gridy = 0
  gbcEvent.insets = new Insets(2, 1, 2, 2)
  gbcEvent.weightx = 0.64
  gbcEvent.weighty = 1.0
  gbcEvent.fill = GridBagConstraints.BOTH

  var item: Option[VisualizationItem] = None

  def update(newItem: Option[VisualizationItem]) {
    logger.debug(s"update $newItem")
    item = newItem
    icon.repaint()
    //    visualization.update()
    sonificationVisusalization.update()
    event.update()
  }

  object icon extends BorderPanel(2, 2) {
    setLayout(new GridBagLayout)
    setPreferredSize(new Dimension(25, 25))
    setMinimumSize(new Dimension(25, 25))
    override def paintComponent(g: Graphics) {
      super.paintComponent(g)

      implicit val g2d = g.asInstanceOf[Graphics2D]
      item foreach { e =>
        val relocated = e.copy(xy = XY(getWidth / 2, getHeight / 2))
        Visualization.draw(relocated, ItemSizeMain)
      }
    }
  }

  object sonificationVisusalization extends BorderPanel(2, 2) {
    setLayout(new BoxLayout(this, BoxLayout.Y_AXIS))
    // description
    private val descSonfification = sf.createButton("Sonification")
    descSonfification.setAlignmentX(Component.CENTER_ALIGNMENT)
    add(descSonfification)
    descSonfification.addActionListener(new ActionListener {
      def actionPerformed(e: ActionEvent) {
        // TODO put in controller and play in background
        for {
          item <- item if item.instrumentDrum.enabled && item.melodyRhythm.enabled
        } {
          val stac = if (item.instrument) {
            s"${StaccatoGenerator.volume(item.volume.getOrElse(127))} ${staccato(item.instrumentDrum.value, item.melodyRhythm.value)}"
          } else {
            s"V9 ${StaccatoGenerator.volume(item.volume.getOrElse(127))} ${staccatoDrumRhythm(item.instrumentDrum.value, item.melodyRhythm.value)}"
          }
          MusicCommons.playAsWav(stac)
        }
      }
    })
    // table
    private val columnNamesSon = Array("Property", "Value", "Mapping")
    private val initialDataSon = Array(
      Array("Instrument/Drum", "", ""),
      Array("Melody/Rhythm", "", ""),
      Array("Panning", "", ""),
      Array("Volume", "", ""))
    private val tableSon = new JTable(new MyTableModel(initialDataSon, columnNamesSon))
    // construct GUI
    add(tableSon.getTableHeader)
    add(tableSon)

    // description
    private val descVisualization = sf.createLabel("Visualization")
    descVisualization.setAlignmentX(Component.CENTER_ALIGNMENT)
    add(descVisualization)
    // table
    private val columnNamesVis = Array("Property", "Value", "Mapping")
    private val initialDataVis = Array(
      Array("Shape", "", ""),
      Array("Color", "", ""))
    private val tableVis = new JTable(new MyTableModel(initialDataVis, columnNamesVis))
    add(tableVis.getTableHeader)
    add(tableVis)

    def update() {
      item foreach { item =>
        val (instrumentMapping, instrumentValue) = if (item.instrumentDrum != null && item.instrumentDrum.enabled) (item.instrumentDrum.value, item.instrumentDrumValue) else ("", "")
        val (melodyMapping, melodyValue) = if (item.melodyRhythm != null && item.melodyRhythm.enabled) (item.melodyRhythm.value, item.melodyRhythmValue) else ("", "")
        val (volumeMapping, volumeValue) = item.volume.map(e => (e, item.volumeValue.get)).getOrElse(("", ""))
        val (panningMapping, panningValue) = item.panning.map(e => (e, item.panningValue)).getOrElse(("", ""))

        val dataSon = Array(
          Array(if (item.instrument) "Instrument" else "Drum Instrument", instrumentValue.toString, instrumentMapping.toString),
          Array(if (item.instrument) "Melody" else "Rhythm", melodyValue.toString, melodyMapping.toString),
          Array("Panning", panningValue.toString, panningMapping.toString),
          Array("Volume", volumeValue.toString, volumeMapping.toString))
        tableSon.getModel.asInstanceOf[MyTableModel].update(dataSon)

        val (shapeMapping, shapeValue) = if (item.shape != null && item.shape.enabled) (item.shape.value.name, item.shapeValue) else ("", "")
        val (colorMapping, colorValue) = if (item.color != null && item.color.enabled) (s"${item.color.value.getRed}/${item.color.value.getGreen}/${item.color.value.getBlue}", item.colorValue.toString) else ("", "")
        val dataVis = Array(
          Array("Shape", shapeValue.toString, shapeMapping.toString),
          Array("Color (RGB)", colorValue.toString, colorMapping.toString))
        tableVis.getModel.asInstanceOf[MyTableModel].update(dataVis)
      }
      descSonfification.setEnabled(item.isDefined)
    }
  }

  object event extends BorderPanel(2, 2) {
    setLayout(new BoxLayout(this, BoxLayout.Y_AXIS))
    // description
    private val desc = sf.createLabel("Event")
    desc.setAlignmentX(Component.CENTER_ALIGNMENT)
    add(desc)
    // data
    private val text = new JTextArea()
    private val pane = new JScrollPane(text)
    text.setEditable(false)
    add(pane)

    def update() {
      item foreach { item =>
        text.setText(serialize(item.event, PrettyPrinter60wide2indent))
      }
    }
  }
  add(icon, gbcIcon)
  add(sonificationVisusalization, gbcSonificationVisualization)
  add(event, gbcEvent)
}
