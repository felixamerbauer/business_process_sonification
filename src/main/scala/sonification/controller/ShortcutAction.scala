package sonification.controller

import java.awt.event.ActionEvent
import java.beans.PropertyChangeListener
import com.typesafe.scalalogging.StrictLogging
import javax.swing.Action
import java.awt.datatransfer.StringSelection
import java.awt.Toolkit

trait ActionHelper extends Action with StrictLogging {
  private var enabled = false;
  override def getValue(key: String): AnyRef = {
    logger.debug(s"getValue key=$key")
    "noIdeaWhatToReturn"
  }

  override def putValue(key: String, value: AnyRef) {
    logger.debug(s"putValue key=$key, value=$value")
  }

  override def setEnabled(b: Boolean) {
    logger.debug(s"setEnabled $b")
    enabled = b
  }

  override def isEnabled(): Boolean = {
    logger.debug(s"isEnabled -> $enabled")
    enabled
  }

  override def addPropertyChangeListener(listener: PropertyChangeListener) {
    logger.debug("addPropertyChangeListener")
  }

  override def removePropertyChangeListener(listener: PropertyChangeListener) {
    logger.debug("addPropertyChangeListener")
  }

}

class ShortcutAction(jumpInSeconds: Int, ctrl: Controller) extends ActionHelper {
  logger.debug(s"ShortcutAction($jumpInSeconds)")
  override def actionPerformed(e: ActionEvent) {
    ctrl.jumpRelative(jumpInSeconds)
  }
}
class CopySettingsToClipboardAction(ctrl: Controller) extends ActionHelper {
  setEnabled(true)
  override def actionPerformed(e: ActionEvent) {
    logger.debug("copy settings to clipboard")
    val txt = ctrl.settings.clipboard(ctrl.log.size)
    val sl  = new StringSelection(txt)
    Toolkit.getDefaultToolkit.getSystemClipboard.setContents(sl, null)
    
  }
}
