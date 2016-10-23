package sonification.ui.right

import java.awt.Dimension
import scala.annotation.migration
import scala.collection.JavaConversions.asJavaIterable
import org.processmining.framework.util.ui.widgets.{ BorderPanel, ProMComboBox }
import com.typesafe.scalalogging.StrictLogging
import GlobalMappingPanel.{ sonificationType2String, string2sonificationType }
import javax.swing.{ Box, BoxLayout }
import sonification.controller.{ Controller, InstrumentType, MappingCategory }
import sonification.controller.{ MelodyType, Modifiable, PanningType, Stop, VisualizationType, VolumeType, code2actionListener, enableFor, _ }
import sonification.controller.MappingModel.SonificationTypes
import sonification.ui.sf
import org.deckfour.xes.model.XElement

object GlobalMappingPanel {
  val SonificationTypesPlusNone = SonificationTypes.map(Option(_)) + None

  val sonificationType2String = Map[Option[SonificationType], String](
    Some(InstrumentType) -> "Instrument",
    Some(MelodyType) -> "Melody",
    Some(DrumType) -> "Drum Instrument",
    Some(RhythmType) -> "Rhythm",
    Some(VolumeType) -> "Volume",
    Some(PanningType) -> "Panning",
    None -> "-")
  val string2sonificationType = sonificationType2String.map(_.swap)
}

/** Allows to set the global mapping */
class GlobalMappingPanel(modifiable: Modifiable)(implicit ctrl: Controller) extends TabPanel with StrictLogging {
  import GlobalMappingPanel._

  private def add(category: MappingCategory[_, _ <: XElement], sonification: Option[SonificationType], visualization: Option[VisualizationType]) {
    logger.debug(s"add for $category: ${sonification.getOrElse("-")}/${visualization.getOrElse("-")}")

    add(new BorderPanel(2, 2) {
      setLayout(new BoxLayout(this, BoxLayout.X_AXIS))
      add(Box.createRigidArea(new Dimension(5, 0)))

      // category
      val tf = sf.createLabel(category.toString)
      add(tf)

      // visualization options
      add(Box.createRigidArea(new Dimension(5, 0)))
      add(Box.createHorizontalGlue())
      val box = new ProMComboBox(Array(visualization.getOrElse("-")).asInstanceOf[Array[Object]])
      box.setSelectedIndex(0)
      box.setPreferredSize(box.getMinimumSize)
      box.setEditable(false)
      add(box)

      // sonification options
      add(Box.createRigidArea(new Dimension(5, 0)))
      val sonificationOptions = SonificationTypesPlusNone.filter(e => e.isEmpty || e.exists(_.typ == category.typ))
      val sonificationCombo = new ProMComboBox(sonificationOptions.map(sonificationType2String).toSeq.sorted)
      sonificationCombo.setSelectedItem(sonificationType2String(sonification))
      sonificationCombo.addActionListener {
        ctrl.globalSonificationMapping(category, string2sonificationType(sonificationCombo.getSelectedItem.asInstanceOf[String]))
      }
      sonificationCombo.setPreferredSize(sonificationCombo.getMinimumSize())
      modifiable.modifiables += ((sonificationCombo, enableFor(Stop)))
      add(sonificationCombo)
    })
  }

  def update() {
    val data = for {
      (category, categoryMapping) <- ctrl.settings.mapping.toSeq.sortBy(_._1.toString)
      sonificationTyp = categoryMapping.sonification.map(_.typ)
      visualizationTyp = categoryMapping.visualization.map(_.typ)
    } {
      println(s"xxx $sonificationTyp $visualizationTyp")
      add(category, sonificationTyp, visualizationTyp)
    }
  }
}

