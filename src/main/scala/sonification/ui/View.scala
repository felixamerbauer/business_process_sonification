package sonification.ui

import org.processmining.framework.util.ui.widgets.{ ProMHeaderPanel, ProMSplitPane }

import com.typesafe.scalalogging.StrictLogging

import javax.swing.ToolTipManager
import sonification.controller.Controller
import sonification.ui.left.Left
import sonification.ui.right.Right

/**
 * Constructs the view of the GUI
 * @param ctrl controller
 */
class View(ctrl: Controller) extends ProMHeaderPanel(null) with StrictLogging {
  implicit val implicitCtrl = ctrl
  // default tooltips happen after 750ms
  ToolTipManager.sharedInstance().setInitialDelay(2000)

  // link the controller to this view
  ctrl.setView(this)


  // GUI - main window
  object MainSplitPane extends ProMSplitPane() {
    val left = new Left()
    setLeftComponent(left)
    val right = new Right()
    setRightComponent(right)
    // resizing could be cleverer
    setResizeWeight(0.5)
  }
  add(MainSplitPane)
  ctrl.initView()
}