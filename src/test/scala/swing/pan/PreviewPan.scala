package swing.pan

import java.awt.{ BasicStroke, Graphics, Graphics2D, Point, Transparency }
import java.awt.event.{ MouseEvent, MouseListener, MouseMotionListener }
import java.awt.geom.Rectangle2D
import java.awt.image.BufferedImage

import com.typesafe.scalalogging.StrictLogging

import javax.swing.JPanel

class PreviewPanel extends JPanel with MouseMotionListener with MouseListener with StrictLogging {
  case class Translate(left: Int, right: Int)
  var window: Rectangle2D = _
  var translate: Int = _
  var moved: Int = 0
  var start: Option[Point] = None
  var maxTranslate: Translate = _

//  lazy val pages = {
//    val tmp = Visualization.pageBorders(getWidth() / zoomFactor, zoomFactor)
//    logger.debug(s"$tmp")
//    tmp
//  }
//  lazy val pageWidth = pages.head.endDisp - pages.head.startDisp

  lazy val image: BufferedImage = {
    val gc = getGraphicsConfiguration
    val image = gc.createCompatibleImage(getWidth, getHeight, Transparency.BITMASK)
    val g2 = image.getGraphics.asInstanceOf[Graphics2D]
//    val rowHeight = getHeight / pages.size / 2
//    for ((page, idx) <- pages.zipWithIndex) {
//      g2.setColor(Color.red)
//      g2.fillRect(page.startDisp, idx * rowHeight, page.endDisp - page.startDisp - 2, rowHeight)
//      g2.setColor(Color.blue)
//      g2.fillRect(page.startPos, (idx + pages.size) * rowHeight, page.endPos - page.startPos - 2, rowHeight)
//    }
    g2.dispose()
    image

  }

  def drawWindow(g2: Graphics2D): Rectangle2D = {
    val oldStroke = g2.getStroke()
    val strokeThickness = 4
    g2.setStroke(new BasicStroke(strokeThickness))
//    val rect = new Rectangle2D.Double(strokeThickness / 2 + moved + translate, strokeThickness / 2, pageWidth - strokeThickness, getHeight() - strokeThickness)
//    g2.draw(rect)
    g2.setStroke(oldStroke)
//    rect
    ???
  }
  override def paintComponent(g: Graphics) {
    super.paintComponent(g)
    g.drawImage(image, 0, 0, null)
    val g2 = g.asInstanceOf[Graphics2D]
    window = drawWindow(g2)
  }

  override def mouseDragged(e: MouseEvent) {
    //    logger.debug(s"mouseDragged $e")
    start.foreach(start => {
      if (this.contains(e.getPoint)) {
        val dif = e.getX - start.getX().toInt
        if (dif < 0) {
          translate = Math.max(dif, maxTranslate.left)
        } else {
          translate = Math.min(dif, maxTranslate.right)
        }
      }
      repaint()
    })
  }

  override def mousePressed(e: MouseEvent) {
    def calcTranslate: Translate = {
      val left = -moved
      val right = getWidth() - window.getWidth().toInt - moved - 4
      Translate(left, right)
    }
    logger.debug(s"mousePressed $e")
    if (window.contains(e.getPoint)) {
      start = Some(e.getPoint)
      maxTranslate = calcTranslate
    } else
      start = None
    logger.debug(s"start=$start, maxTranslate=$maxTranslate, moved=$moved")
  }

  override def mouseReleased(e: MouseEvent) {
    logger.debug(s"mouseReleased $e")
    start foreach { _ =>
      moved += translate
      translate = 0
      logger.debug(s"maxTranslate=$maxTranslate, moved=$moved")
    }
  }

  override def mouseMoved(e: MouseEvent) {}
  override def mouseClicked(e: MouseEvent) { }
  override def mouseEntered(e: MouseEvent) {  }
  override def mouseExited(e: MouseEvent) {  }
  addMouseListener(this)
  addMouseMotionListener(this)

}