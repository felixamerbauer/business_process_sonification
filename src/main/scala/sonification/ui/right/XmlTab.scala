package sonification.ui.right

import java.awt.event.{ ActionEvent, ActionListener }

import scala.concurrent.ExecutionContext.Implicits
import scala.concurrent.Future

import javax.swing.{ JTextArea, RootPaneContainer }
import sonification.controller.Controller
import sonification.openxes.OpenXESHelper.{ PrettyPrinter120wide4indent, serialize }
import sonification.ui.CursorDeluxe
import sonification.ui.SwingHelper.edt

class XmlTab(implicit ctrl: Controller) extends TabPanel with CursorDeluxe with ActionListener {
  // TODO use swingworker
  import scala.concurrent.ExecutionContext.Implicits.global

  private var tla: Option[RootPaneContainer] = None

  private val textArea = new JTextArea

  private var xml: Option[String] = None

  private def recalculate() {
    xml = Some(serialize(ctrl.traces.log, PrettyPrinter120wide4indent))
  }

  def update() {
    xml = None
  }

  // initial: set to dirty and   
  add(textArea)

  private def fillContent() {
    waitCursor(tla.get)
    Future(recalculate()).onComplete { _ =>
      edt {
        xml foreach textArea.setText
        normalCursor(tla.get)
      }
    }
  }

  def actionPerformed(e: ActionEvent) {
    if (xml != null && xml.isEmpty && tla != null && tla.isDefined) {
      fillContent()
    }
  }

  override def repaint() {
    if (tla != null && tla.isEmpty) {
      getTopLevelAncestor match {
        case rpc: RootPaneContainer =>
          tla = Some(rpc)
          fillContent()
        case _ =>
      }
    }
    super.repaint()
  }

}