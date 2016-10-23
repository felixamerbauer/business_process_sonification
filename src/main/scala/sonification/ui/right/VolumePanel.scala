package sonification.ui.right

import java.util.Hashtable

import com.typesafe.scalalogging.StrictLogging

import javax.swing.{ BoxLayout, JLabel, JPanel, JSlider }
import javax.swing.SwingConstants.{ CENTER, HORIZONTAL }
import sonification.controller.{ Controller, Modifiable, Stop, code2changeListener, enableFor }
import sonification.ui.sf

class VolumePanel(modifiable: Modifiable)(implicit ctrl: Controller) extends JPanel with StrictLogging {
  setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS))
  val label = sf.createLabel("Volume")
  label.setHorizontalAlignment(CENTER)

  val slider = new JSlider(HORIZONTAL, 0, 127, 127)
  slider.setMajorTickSpacing(16)
  slider.setMinorTickSpacing(8)
  val labelTable = new Hashtable[Integer, JLabel]()
  labelTable.put(0, new JLabel("Min"))
//  labelTable.put(64,sf.createLabel("Volume"))
  labelTable.put(127, new JLabel("Max"))
  slider.setLabelTable(labelTable)

  slider.setPaintTicks(true)
  slider.setPaintLabels(true)
  slider.addChangeListener(code2changeListener(if (!slider.getValueIsAdjusting) ctrl.volume(slider.getValue)))

  modifiable.modifiables += ((slider, enableFor(Stop)))

  add(label)
  add(slider)
}