package sonification.ui.right

import java.awt.{ Component, Dimension }
import java.awt.event.{ ActionEvent, ActionListener }
import scala.Array.canBuildFrom
import scala.collection.JavaConversions.asJavaIterable
import org.processmining.framework.util.ui.widgets.{ BorderPanel, ProMComboBox, ProMTextField }
import com.typesafe.scalalogging.StrictLogging
import javax.swing.{ Box, BoxLayout, JButton, JColorChooser, JTable }
import sonification.controller.{ AllShapes, CategoryMapping, ColorMapping, Continuous, Controller, InstrumentMapping, Literal, LiteralMapping, MappingCategory, MelodyMapping, Modifiable, PanningMapping, ShapeMapping, Stop, VolumeMapping, code2actionListener, documentListener, enableFor, melodyValidator, rhtythmValidator }
import sonification.music.MusicCommons
import sonification.ui.sf
import sonification.controller.DrumMapping
import sonification.controller.RhythmMapping

class ConcreteMappingTab(category: MappingCategory[_, _], mapping: CategoryMapping, modifiable: Modifiable)(implicit ctrl: Controller) extends TabPanel with StrictLogging {
  logger.debug(s"() for $category: ${mapping.visualization.getOrElse("-")} and ${mapping.sonification.getOrElse("-")}")

  def literal(sonification: Option[LiteralMapping[_]], visualization: Option[LiteralMapping[_]]) {
    val literalMapping = sonification.getOrElse(visualization.get)
    literalMapping.values.keySet.toSeq.sorted foreach { key =>
      add(new BorderPanel(2, 2) {
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS))
        val keyEnabled = literalMapping.isEnabled(key)
        add(Box.createRigidArea(new Dimension(5, 0)))
        // enable disable
        val checkbox = sf.createCheckBox("", keyEnabled)
        add(checkbox)
        checkbox.addActionListener {
          ctrl.literalMapping(sonification, visualization, key, checkbox.isSelected)
          // TODO redraw panel

        }
        add(Box.createHorizontalGlue)
        // key name
        val label = sf.createLabel(key)
        add(label)
        add(Box.createHorizontalGlue)

        // visualization mapping
        mapping.visualization foreach (_ match {
          case s: ShapeMapping =>
            val combo = new ProMComboBox[String](asJavaIterable(AllShapes.map(_.name)))
            combo.setPreferredSize(combo.getMinimumSize)
            val shape = s.values(key).value
            combo.setSelectedItem(shape.name)
            combo.addActionListener {
              ctrl.literalMapping(s, key, AllShapes.find(_.name == combo.getSelectedItem.toString).get)
            }
            modifiable.modifiables += ((combo, enableFor(Stop)))
            add(combo)
          case c: ColorMapping =>
            val color = c.values(key).value
            val cc = new JButton(" ")
            cc.setBackground(color)
            cc.addActionListener(new ActionListener() {
              override def actionPerformed(e: ActionEvent) {
                val selection = JColorChooser.showDialog(e.getSource.asInstanceOf[Component].getParent, s"Color for $key", color)
                ctrl.literalMapping(c, key, selection)
                cc.setBackground(selection)
              }
            })
            add(cc)
            modifiable.modifiables += ((cc, enableFor(Stop)))
            add(Box.createRigidArea(new Dimension(5, 0)))
        })

        // sonification mapping
        mapping.sonification foreach (_ match {
          case i: InstrumentMapping =>
            val instrument = i.values(key).value
            // instrument selector
            val instrumentSelector = new ProMComboBox[String](asJavaIterable(MusicCommons.JFugueInstrumentStrings.sorted))
            instrumentSelector.setPreferredSize(instrumentSelector.getMinimumSize())
            modifiable.modifiables += ((instrumentSelector, enableFor(Stop)))
            instrumentSelector.setSelectedItem(instrument)
            instrumentSelector.addActionListener {
              ctrl.literalMapping(i, key, instrumentSelector.getSelectedItem.toString)
            }
            add(instrumentSelector)
            // instrument prelisten
            val instrumentPrelisten = sf.createButton(">")
            instrumentPrelisten.addActionListener {
              ctrl.prelistenInstrument(instrumentSelector.getSelectedItem().toString)
            }
            add(instrumentPrelisten)
          case d: DrumMapping =>
            val drum = d.values(key).value
            // drum selector
            val drumSelector = new ProMComboBox[String](asJavaIterable(MusicCommons.Drums.sorted))
            drumSelector.setPreferredSize(drumSelector.getMinimumSize())
            modifiable.modifiables += ((drumSelector, enableFor(Stop)))
            drumSelector.setSelectedItem(drum)
            drumSelector.addActionListener {
              ctrl.literalMapping(d, key, drumSelector.getSelectedItem.toString)
            }
            add(drumSelector)
            // drum prelisten
            val drumPrelisten = sf.createButton(">")
            drumPrelisten.addActionListener {
              ctrl.prelistenDrum(drumSelector.getSelectedItem().toString)
            }
            add(drumPrelisten)

          case m: MelodyMapping =>
            val melody = m.values(key).value
            // melody free text form
            val melodyText = new ProMTextField(melody)
            melodyText.setPreferredSize(new Dimension(200, 30))
            melodyText.getDocument().addDocumentListener(documentListener(melodyText, m, key, ctrl.literalMapping, 500))
            melodyText.getDocument().addDocumentListener(melodyValidator(melodyText, 1000, ctrl))
            // TODO disabling doesn't work for text fields
            modifiable.modifiables += ((melodyText, enableFor(Stop)))
            add(melodyText)
            // melody prelisten 120 bpm
            val melodyPrelisten120bpm = sf.createButton(">")
            melodyPrelisten120bpm.setToolTipText("Prelisten with 120bpm")
            melodyPrelisten120bpm.addActionListener {
              ctrl.prelistenMelody(melodyText.getText, 1)
            }
            add(melodyPrelisten120bpm)
            // melody prelisten actual speed
            val melodyPrelistenActualSpeed = sf.createButton(">>")
            melodyPrelistenActualSpeed.setToolTipText("Prelisten with actual speed")
            melodyPrelistenActualSpeed.addActionListener {
              ctrl.prelistenMelody(melodyText.getText, ctrl.settings.speed)
            }
            add(melodyPrelistenActualSpeed)

          case r: RhythmMapping =>
            val rhythm = r.values(key).value
            // rhythm free text form
            val rhythmText = new ProMTextField(rhythm)
            rhythmText.setPreferredSize(new Dimension(200, 30))
            rhythmText.getDocument().addDocumentListener(documentListener(rhythmText, r, key, ctrl.literalMapping, 500))
            rhythmText.getDocument().addDocumentListener(rhtythmValidator(rhythmText, 1000, ctrl))
            // TODO disabling doesn't work for text fields
            modifiable.modifiables += ((rhythmText, enableFor(Stop)))
            add(rhythmText)
            // rhythm prelisten 120 bpm
            val rhythmPrelisten120bpm = sf.createButton(">")
            rhythmPrelisten120bpm.setToolTipText("Prelisten with 120bpm")
            rhythmPrelisten120bpm.addActionListener {
              ctrl.prelistenRhythm(rhythmText.getText, 1)
            }
            add(rhythmPrelisten120bpm)
            // rhythm prelisten actual speed
            val rhythmPrelistenActualSpeed = sf.createButton(">>")
            rhythmPrelistenActualSpeed.setToolTipText("Prelisten with actual speed")
            rhythmPrelistenActualSpeed.addActionListener {
              ctrl.prelistenRhythm(rhythmText.getText, ctrl.settings.speed)
            }
            add(rhythmPrelistenActualSpeed)
          case _ =>
        })
      })
    }
  }

  def continuous() {
    def table(columnNames: Array[String], data: Map[Double, Int]) {
      val tableData = data.toArray.map {
        case (value, mapped) => Array(value.toString, mapped.toString)
      }
      val table = new JTable(new MyTableModel(tableData, columnNames))
      add(table.getTableHeader)
      add(table)
    }

    mapping.sonification.get match {
      case v: VolumeMapping =>
        val columnNames = Array("Cost", "Volume")
        table(columnNames, v.values)

      case p: PanningMapping =>
        val columnNames = Array("Cost", "Panning")
        table(columnNames, p.values)
      case _ =>
    }

  }

  // check if continuous or literal mapping category
  category.typ match {
    case Continuous => continuous()
    case Literal =>
      // determine keys either from sonification or visualization, at least one needs to be defined
      val keyHolder = (mapping.sonification, mapping.visualization) match {
        case (Some(s: LiteralMapping[_]), Some(v: LiteralMapping[_])) => literal(Some(s), Some(v))
        case (Some(s: LiteralMapping[_]), _) => literal(Some(s), None)
        case (_, Some(v: LiteralMapping[_])) => literal(None, Some(v))
        case _ => ??? // TODO
      }
  }

}