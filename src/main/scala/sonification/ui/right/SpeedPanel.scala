package sonification.ui.right

import java.awt.{ GridBagConstraints, GridBagLayout }
import java.awt.event.{ ActionEvent, ActionListener, ItemEvent, ItemListener }
import java.util.Hashtable

import javax.swing.{ JLabel, JPanel, JSlider }
import javax.swing.SwingConstants.HORIZONTAL
import sonification.controller.{ Controller, Modifiable, Stop, code2changeListener, enableFor }
import sonification.ui.right.Player.{ DefaultDuration, DefaultDurationEventSpeed, DefaultSpeed, DurationEventSpeeds, Durations, Speeds }
import sonification.ui.sf

class SpeedPanel(modifiable: Modifiable)(implicit ctrl: Controller) extends JPanel {
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

  // 1st row
  val speedDuration = sf.createButton("Speed/Duration")
  private def durationUI() {
    remove(speedSlider)
    remove(eventSpeedSlider)
    add(durationSlider, speedDurationSliderGbc)
    add(eventDurationSlider, eventSpeedDurationSliderGbc)
    totalSpeedDurationLabel.setText("Total Duration")
  }
  // workaround
  def enabaleDuration() {
    durationUI()
    revalidate()
    repaint()
  }

  speedDuration.addActionListener(new ActionListener {
    override def actionPerformed(e: ActionEvent) {
      // duration -> speed
      if (ctrl.settings.duration.isDefined) {
        println("duration -> speed")
        remove(durationSlider)
        remove(eventDurationSlider)
        add(speedSlider, speedDurationSliderGbc)
        add(eventSpeedSlider, eventSpeedDurationSliderGbc)
        totalSpeedDurationLabel.setText("Total Speed")
      } // speed -> duration
      else {
        println("speed -> duration")
        durationUI()
      }
      revalidate()
      repaint()
      ctrl.switchSpeedDuration()
    }
  })
  modifiable.modifiables += ((speedDuration, enableFor(Stop)))

  add(speedDuration, gbc(gridx = 2, gridy = 0, weightx = 1, gridwidth = 1, fill = GridBagConstraints.VERTICAL))

  // 2nd row
  val totalSpeedDurationLabel = sf.createLabel("Total Speed")
  // speed slider
  val speedSlider = new JSlider(HORIZONTAL, 0, Speeds.last.slider, DefaultSpeed.slider)
  speedSlider.setMajorTickSpacing(20)
  speedSlider.setMinorTickSpacing(10)
  speedSlider.setSnapToTicks(true)
  val speedLabelTable = new Hashtable[Integer, JLabel]()
  Speeds foreach { e =>
    speedLabelTable.put(e.slider, new JLabel(e.label))
  }
  speedSlider.setLabelTable(speedLabelTable)
  speedSlider.setPaintTicks(true)
  speedSlider.setPaintLabels(true)
  // TODO simplify
  speedSlider.addChangeListener(code2changeListener(if (!speedSlider.getValueIsAdjusting) ctrl.speed(speedSlider.getValue)))

  // duration slider
  val durationSlider = new JSlider(HORIZONTAL, 0, Durations.last.slider, DefaultDuration.slider)
  durationSlider.setMajorTickSpacing(20)
  durationSlider.setMinorTickSpacing(10)
  durationSlider.setSnapToTicks(true)
  val durationLabelTable = new Hashtable[Integer, JLabel]()
  Durations foreach { e =>
    durationLabelTable.put(e.slider, new JLabel(e.label))
  }
  durationSlider.setLabelTable(durationLabelTable)
  durationSlider.setPaintTicks(true)
  durationSlider.setPaintLabels(true)
  durationSlider.addChangeListener(code2changeListener(if (!durationSlider.getValueIsAdjusting) ctrl.duration(durationSlider.getValue)))

  modifiable.modifiables += ((speedSlider, enableFor(Stop)))
  modifiable.modifiables += ((durationSlider, enableFor(Stop)))

  add(totalSpeedDurationLabel, gbc(gridx = 1, gridy = 1, weightx = 0.2))
  val speedDurationSliderGbc = gbc(gridx = 2, gridy = 1, weightx = 0.7)
  add(speedSlider, speedDurationSliderGbc)

  // 3rd row
  val checkbox = sf.createCheckBox("", true)
  checkbox.addItemListener(new ItemListener {
    override def itemStateChanged(e: ItemEvent) {
      val selected = e.getStateChange == ItemEvent.SELECTED
      ctrl.eventOrEventDurationSpeed(selected)
    }
  })
  val eventSpeedLabel = sf.createLabel("Event Speed")
  // slider for event speed
  val eventSpeedSlider = new JSlider(HORIZONTAL, 0, Speeds.last.slider, DefaultSpeed.slider)
  eventSpeedSlider.setMajorTickSpacing(20)
  eventSpeedSlider.setMinorTickSpacing(10)
  eventSpeedSlider.setSnapToTicks(true)
  val eventSpeedLabelTable = new Hashtable[Integer, JLabel]()
  Speeds foreach { e =>
    eventSpeedLabelTable.put(e.slider, new JLabel(e.label))
  }
  eventSpeedSlider.setLabelTable(eventSpeedLabelTable)
  eventSpeedSlider.setPaintTicks(true)
  eventSpeedSlider.setPaintLabels(true)
  eventSpeedSlider.setEnabled(true)
  eventSpeedSlider.addChangeListener(code2changeListener(if (!eventSpeedSlider.getValueIsAdjusting) ctrl.eventSpeed(eventSpeedSlider.getValue)))
  // slider for event duration
  val eventDurationSlider = new JSlider(HORIZONTAL, 0, DurationEventSpeeds.last.slider, DefaultDurationEventSpeed.slider)
  eventDurationSlider.setMajorTickSpacing(20)
  eventDurationSlider.setMinorTickSpacing(10)
  eventDurationSlider.setSnapToTicks(true)
  val eventDurationSpeedLabelTable = new Hashtable[Integer, JLabel]()
  DurationEventSpeeds foreach { e =>
    eventDurationSpeedLabelTable.put(e.slider, new JLabel(e.label))
  }
  eventDurationSlider.setLabelTable(eventDurationSpeedLabelTable)
  eventDurationSlider.setPaintTicks(true)
  eventDurationSlider.setPaintLabels(true)
  eventDurationSlider.setEnabled(true)
  eventDurationSlider.addChangeListener(code2changeListener(if (!eventDurationSlider.getValueIsAdjusting) ctrl.eventDurationSpeed(eventDurationSlider.getValue)))

  add(checkbox, gbc(gridx = 0, gridy = 2, weightx = 0.1))
  add(eventSpeedLabel, gbc(gridx = 1, gridy = 2, weightx = 0.2))
  val eventSpeedDurationSliderGbc = gbc(gridx = 2, gridy = 2, weightx = 0.7)
  add(eventSpeedSlider, eventSpeedDurationSliderGbc)

}
