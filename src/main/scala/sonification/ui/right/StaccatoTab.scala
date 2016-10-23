package sonification.ui.right

import java.awt.event.{ ActionEvent, ActionListener }

import scala.concurrent.ExecutionContext.Implicits
import scala.concurrent.Future

import javax.swing.{ JTextArea, RootPaneContainer }
import sonification.controller.Controller
import sonification.ui.CursorDeluxe
import sonification.ui.SwingHelper.edt

class StaccatoTab(implicit ctrl: Controller) extends TabPanel with CursorDeluxe with ActionListener {
  // TODO use swingworker
  import scala.concurrent.ExecutionContext.Implicits.global

  private var tla: Option[RootPaneContainer] = None

  val textArea = new JTextArea()

  // initial: set to dirty and add the empty GUI element
  add(textArea)

  private def fillContent() {
    waitCursor(tla.get)
    Future(ctrl.calculateStaccato()).onComplete { _ =>
      edt {
        ctrl.staccato foreach { e =>
          val withLineBreaks = e.replaceAll("""( V)|( L\d+)""", "\n$0")
          textArea.setText(withLineBreaks)
        }
        normalCursor(tla.get)
      }
    }
  }

  def actionPerformed(e: ActionEvent) {
    if (ctrl.staccato != null && ctrl.staccato.isEmpty && tla != null && tla.isDefined) {
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