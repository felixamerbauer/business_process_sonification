package sonification.ui.left

import java.awt.{ BasicStroke, Graphics, Graphics2D }
import java.awt.event.{ MouseEvent, MouseListener, MouseMotionListener }
import java.awt.geom.Rectangle2D
import java.awt.image.BufferedImage

import com.typesafe.scalalogging.StrictLogging

import javax.swing.JPanel
import sonification.controller.{ Controller, FitPage, Modifiable, Play, Stop }
import sonification.ui.left.VisusalizationCalculator.ItemSizeOverview

object VisualizationOverview {
  var dirty = true
}

class VisualizationOverview(ctrl: Controller) extends JPanel with MouseMotionListener with MouseListener with Modifiable with StrictLogging {

  case class Translate(left: Int, right: Int)

  private var pageBorders: Rectangle2D = _
  private var translate: Int = _
  private var dragMoved = 0
  var start = 0F
  private var startX: Option[Int] = None
  private var maxTranslate: Translate = _
  private var image: BufferedImage = _

  // environment variables
  private var curWidth: Int = _
  private var curHeight: Int = _

  def updateUISettings() {
    logger.debug("updateUI")
    image = Visualization.calculateBufferedImage(ctrl, getGraphicsConfiguration, getWidth, getHeight, ItemSizeOverview)._2
  }

  private def drawPageBorders(g2: Graphics2D, x: Int) {
    val oldStroke = g2.getStroke()
    val strokeThickness = 1
    g2.setStroke(new BasicStroke(strokeThickness))
    val rX = x
    val rY = 0
    val rW = Math.round(curWidth.toFloat / ctrl.settings.zoomLevel.zoomFactor) - strokeThickness
    val rH = curHeight - strokeThickness
    // logger.debug(s"drawWindow x=$x, rX=$rX, rY=$rY, rH=$rH, rW=$rW")
    pageBorders = new Rectangle2D.Double(rX, rY, rW, rH)
    g2.draw(pageBorders)
    g2.setStroke(oldStroke)
  }

  override def paintComponent(g: Graphics) {
    super.paintComponent(g)
    if (curWidth != getWidth || curHeight != getHeight || VisualizationOverview.dirty) {
      // special handling fit page
      if (ctrl.settings.zoomLevel == FitPage) {
        start = 0
        dragMoved = 0
      }
      curWidth = getWidth
      curHeight = getHeight
      logger.debug(s"dirty")
      VisualizationOverview.dirty = false
      updateUISettings()
    }
    g.drawImage(image, 0, 0, null)

    val zoomFactor = ctrl.settings.zoomLevel.zoomFactor

    val g2 = g.asInstanceOf[Graphics2D]
    if (ctrl.state == Play) {
      // check if current playback position fits in current page borders
      if (ctrl.fraction < start || ctrl.fraction > start + 1 / zoomFactor.toFloat) {
        // just move it to the current playback position but check that we aren't moving to far
        val max = 1 - 1 / zoomFactor.toFloat
        start = Math.min(ctrl.fraction, max)
        logger.debug(s"Play moves overview to start of current playback fraction=${ctrl.fraction}, start=$start, max=$max")
      }
      drawPageBorders(g2, Math.round(start * curWidth))
    } else {
      drawPageBorders(g2, dragMoved + translate)
    }
  }

  override def mouseDragged(e: MouseEvent) {
    //    logger.debug(s"mouseDragged $e")
    startX.foreach(startX => {
      if (this.contains(e.getPoint)) {
        val dif = e.getX - startX
        if (dif < 0) {
          translate = Math.max(dif, maxTranslate.left)
        } else {
          translate = Math.min(dif, maxTranslate.right)
        }
      }
      ctrl.view.MainSplitPane.left.repaint()
    })
  }

  private def startLastPage = Math.round(curWidth / ctrl.settings.zoomLevel.zoomFactor.toFloat * (ctrl.settings.zoomLevel.zoomFactor - 1))

  override def mousePressed(e: MouseEvent) {
    def calcTranslate: Translate = {
      // logger.debug(s"dragMoved $dragMoved startLastPage $startLastPage curWidth $curWidth zoomFactor ${ctrl.settings.zoomLevel.zoomFactor}")
      val left = -dragMoved
      val right = startLastPage - dragMoved
      Translate(left, right)
    }
    // logger.debug(s"mousePressed ${e.getPoint}")
    if (pageBorders.contains(e.getPoint)) {
      startX = Some(e.getX)
      maxTranslate = calcTranslate
    } else
      startX = None
    // logger.debug(s"startX=$startX, maxTranslate=$maxTranslate, dragMoved=$dragMoved")
  }

  override def mouseReleased(e: MouseEvent) {
    // logger.debug(s"mouseReleased ${e.getPoint}")
    startX foreach { _ =>
      dragMoved += translate
      start = dragMoved.toFloat / curWidth
      translate = 0
      // logger.debug(s"mouseReleased dragMoved=$dragMoved start=$start curWidth=$curWidth")
      ctrl.view.MainSplitPane.left.visualization.repaint()
    }
  }

  override def mouseMoved(e: MouseEvent) {}
  override def mouseClicked(e: MouseEvent) {}
  override def mouseEntered(e: MouseEvent) {}
  override def mouseExited(e: MouseEvent) {}

  addMouseListener(this)
  addMouseMotionListener(this)

  executables += { (oldState, newState) =>
    (oldState, newState) match {
      case (_, Stop) =>
        dragMoved = 0
        start = 0
        addMouseListener(this)
        addMouseMotionListener(this)
      case (Stop, Play) =>
        removeMouseListener(this)
        removeMouseMotionListener(this)
      //        dragMoved = 0
      //        start = 0
      case _ =>
    }
  }

}