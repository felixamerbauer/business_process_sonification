package sonification.ui

import java.awt.Cursor

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits
import scala.concurrent.Future
import scala.concurrent.duration.Duration

import SwingHelper.{ DefaultCursor, WaitCursor }
import javax.swing.{ JComponent, RootPaneContainer, SwingUtilities }

object SwingHelper {
  // TODO use swingworker
  import scala.concurrent.ExecutionContext.Implicits.global

  def edt(code: => Unit) {
    SwingUtilities.invokeLater(new Runnable() {
      override def run() { code }
    })
  }
  val WaitCursor = Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR)
  val DefaultCursor = Cursor.getDefaultCursor()

  def waitCursor(component: JComponent) {
    assert(SwingUtilities.isEventDispatchThread)
    val root = component.getTopLevelAncestor().asInstanceOf[RootPaneContainer]
    root.getGlassPane.setCursor(WaitCursor)
    root.getGlassPane.setVisible(true)
  }
  def normalCursor(component: JComponent) {
    assert(SwingUtilities.isEventDispatchThread)
    val root = component.getTopLevelAncestor().asInstanceOf[RootPaneContainer]
    root.getGlassPane.setCursor(DefaultCursor)
    root.getGlassPane.setVisible(false)
  }

  def doBackgroundWorkWithWaitCursor[R](component: JComponent, code: => R): R = {
    waitCursor(component)
    val result = Await.result(Future(code), Duration.Inf)
    normalCursor(component)
    result
  }
}

trait CursorDeluxe {
  this: JComponent =>

  def waitCursor() {
    assert(SwingUtilities.isEventDispatchThread)
    val root = getTopLevelAncestor().asInstanceOf[RootPaneContainer]
    root.getGlassPane.setCursor(WaitCursor)
    root.getGlassPane.setVisible(true)

  }

  def waitCursor(root:RootPaneContainer) {
    assert(SwingUtilities.isEventDispatchThread)
    root.getGlassPane.setCursor(WaitCursor)
    root.getGlassPane.setVisible(true)

  }

  def normalCursor() {
    assert(SwingUtilities.isEventDispatchThread)
    val root = getTopLevelAncestor().asInstanceOf[RootPaneContainer]
    root.getGlassPane.setCursor(DefaultCursor)
    root.getGlassPane.setVisible(false)
  }

  def normalCursor(root:RootPaneContainer) {
    assert(SwingUtilities.isEventDispatchThread)
    root.getGlassPane.setCursor(DefaultCursor)
    root.getGlassPane.setVisible(false)
  }
}