package sonification.ui.right

import java.awt.{ GridBagConstraints, GridBagLayout }
import java.util.Hashtable

import javax.swing.{ JFrame, JLabel, JPanel, JSlider }
import javax.swing.SwingConstants.HORIZONTAL
import javax.swing.SwingUtilities
import sonification.controller.Controller
import sonification.ui.right.Player.{ DefaultSpeed, Speeds }
import sonification.ui.sf

object GBLTest extends App {

  val doCreateAndShowGUI = new Runnable() {
    def run() {
      val f = new JFrame("ActivityIcons")
      f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE)
      f.setSize(600, 200)
      //    checkerboard.add(new MovingButton("Start Animation"))
      implicit val ctrl: Controller = null
      f.add(new GBL)
      f.setVisible(true)
      f.setResizable(true)
    }
  }
  SwingUtilities.invokeLater(doCreateAndShowGUI)
}
class GBL extends JPanel {
  setLayout(new GridBagLayout)

  def gbc(gridx: Int, gridy: Int, weightx: Double, weighty: Double = 1, gridwidth: Int = 1, fill: Int = GridBagConstraints.BOTH): GridBagConstraints = {
    val tmp = new GridBagConstraints
    tmp.gridx = gridx
    tmp.gridy = gridy
    tmp.weightx = weightx
    tmp.weighty = weighty
    tmp.gridwidth = gridwidth
    tmp.fill = fill
    tmp
  }

  def slider: JSlider = {
    val speed = new JSlider(HORIZONTAL, 0, Speeds.last.slider, DefaultSpeed.slider)
    speed.setMajorTickSpacing(20)
    speed.setMinorTickSpacing(10)
    speed.setSnapToTicks(true)
    val speedLabelTable = new Hashtable[Integer, JLabel]()
    Speeds foreach { e =>
      speedLabelTable.put(e.slider, new JLabel(e.label))
    }
    speed.setLabelTable(speedLabelTable)
    speed.setPaintTicks(true)
    speed.setPaintLabels(true)
    speed
  }

  // 1st row
  val speedDuration = sf.createButton("Speed/Duration")
  
  add(speedDuration, gbc(gridx = 0, gridy = 0, weightx = 1, gridwidth = 3))

  // 2nd row
  val totalSpeedDurationLabel = sf.createLabel("Total Speed")
  val totalSpeedDurationSlider = slider

  add(totalSpeedDurationLabel,gbc(gridx = 1, gridy = 1, weightx = 0.2))
  add(totalSpeedDurationSlider,gbc(gridx = 2, gridy = 1, weightx = 0.7))

  // 3rd row
  val cb = sf.createCheckBox("", false)
  val eventSpeedLabel = sf.createLabel("Event Speed")
  val eventSpeedSlider = slider

  add(cb,gbc(gridx = 0, gridy = 2, weightx = 0.1))
  add(eventSpeedLabel,gbc(gridx = 1, gridy = 2, weightx = 0.2))
  add(eventSpeedSlider,gbc(gridx = 2, gridy = 2, weightx = 0.7))
}
