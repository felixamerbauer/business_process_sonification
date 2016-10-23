package sonification.ui.left

import java.awt.{ BorderLayout, Dimension, GridBagConstraints, GridBagLayout, Insets }

import org.processmining.framework.util.ui.widgets.ProMHeaderPanel

import com.typesafe.scalalogging.StrictLogging

import javax.swing.{ JComboBox, JLabel, JPanel }
import sonification.controller.{ Controller, FitPage, Modifiable, Stop, ZoomLevels, code2actionListener, enableFor }

// GUI - left part (visualization)
class Left(implicit ctrl: Controller) extends ProMHeaderPanel(null) with Modifiable with StrictLogging {
  setLayout(new GridBagLayout)

  // Panel with current position and total duration
  object zoomPositionDuration extends JPanel {
    setLayout(new BorderLayout())
    // zoom
    val zoomCombo = new JComboBox[String](ZoomLevels.map(_.label).toArray)
    zoomCombo.setSelectedItem(FitPage.label)
    zoomCombo.addActionListener {
      ctrl.zoomLevel(ZoomLevels.find(_.label == zoomCombo.getSelectedItem().asInstanceOf[String]).get)
    }
    //    zoomCombo.setPreferredSize(zoomCombo.getMinimumSize())
    modifiables += ((zoomCombo, enableFor(Stop)))
    add(zoomCombo, BorderLayout.WEST)

    private val defaultValue = "00:00:00:000"
    val currentPosition = new JLabel(defaultValue)
    def setToBeginning() {
      currentPosition.setText(defaultValue)
    }
    setMaximumSize(new Dimension(2000, 30))
    val totalDuration = new JLabel(defaultValue)
    add(currentPosition, BorderLayout.CENTER)
    add(totalDuration, BorderLayout.EAST)
  }

  object zoom extends JPanel {
    setLayout(new BorderLayout())
    setMaximumSize(new Dimension(2000, 30))
    val zoomCombo = new JComboBox[String](ZoomLevels.map(_.label).toArray)
    zoomCombo.setSelectedItem(FitPage.label)
    zoomCombo.addActionListener {
      ctrl.zoomLevel(ZoomLevels.find(_.label == zoomCombo.getSelectedItem().asInstanceOf[String]).get)
    }
    zoomCombo.setPreferredSize(zoomCombo.getMinimumSize())
    modifiables += ((zoomCombo, enableFor(Stop)))
    add(zoomCombo, BorderLayout.WEST)
  }

  def gbc(gridy: Int, weighty: Double = 1, gridwidth: Int = 1, fill: Int = GridBagConstraints.BOTH, insetTop: Int = 0, insetBottom: Int = 0): GridBagConstraints = {
    val tmp = new GridBagConstraints
    tmp.gridx = 0
    tmp.gridy = gridy
    tmp.weightx = 1
    tmp.weighty = weighty
    tmp.gridwidth = gridwidth
    tmp.fill = fill
    // top, left, bottom, right
    tmp.insets = new Insets(insetTop, 0, insetBottom, 0)
    tmp
  }

  val visualization = new Visualization(ctrl)
  add(visualization, gbc(gridy = 0, weighty = 8 / 9d))
  val visualizationOverview = new VisualizationOverview(ctrl)
  add(visualizationOverview, gbc(gridy = 1, weighty = 1 / 9d, insetTop = 5, insetBottom = 5))
  add(zoomPositionDuration, gbc(gridy = 2, weighty = 0))
}

