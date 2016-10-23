package sonification.ui.right

import java.awt.Dimension

import scala.collection.mutable.Buffer

import org.processmining.framework.util.ui.widgets.BorderPanel

import javax.swing.{ Box, BoxLayout, JCheckBox }
import sonification.controller.{ Controller, Modifiable, Stop, code2actionListener, enableFor }
import sonification.ui.sf

class TracesSelection(modifiable: Modifiable)(implicit ctrl: Controller) extends TabPanel {
  private val checkboxes = Buffer[JCheckBox]()
  private val selectionPanels = Buffer[BorderPanel]()
  def removeSelectionPanels() = selectionPanels foreach remove
  // select deselect all
  add(new BorderPanel(2, 2) {
    setLayout(new BoxLayout(this, BoxLayout.X_AXIS))
    add(Box.createRigidArea(new Dimension(5, 0)))
    val bSelect = sf.createButton("Select All")
    bSelect.addActionListener { ctrl.tracesSelectionAll() }
    add(bSelect)
    add(Box.createRigidArea(new Dimension(5, 0)))
    val bDeselect = sf.createButton("Deselect All")
    bDeselect.addActionListener { ctrl.tracesSelectionNone() }
    add(bDeselect)
    add(Box.createHorizontalGlue())
  })
  // trace selection
  def add(idx: Int, description: String,selected:Boolean) {
    val panel =new BorderPanel(2, 2) {
      setLayout(new BoxLayout(this, BoxLayout.X_AXIS))
      // checkbox
      val checkBox: JCheckBox = sf.createCheckBox("", true)
      checkboxes += checkBox
      checkBox.setSelected(selected)
      checkBox.addActionListener { ctrl.traceSelection(idx, checkBox.isSelected()) }
      checkBox.setPreferredSize(new Dimension(50, 30))
      modifiable.modifiables += ((checkBox, enableFor(Stop)))
      add(checkBox)
      add(Box.createRigidArea(new Dimension(5, 0)))
      // description
      val tf1 = sf.createLabel(description)
      tf1.setPreferredSize(new Dimension(500, 30))
      add(tf1)
      add(Box.createHorizontalGlue())
    }
    selectionPanels += panel
    add(panel)
  }
  def toggle(selected: Boolean) {
    checkboxes.foreach(_.setSelected(selected))
  }
}