package swing.pan

import java.awt.{ Color, Graphics, Graphics2D, Transparency }
import java.awt.event.{ ActionEvent, ActionListener }
import java.lang.Math.{ ceil, max }

import scala.annotation.tailrec
import scala.util.Random

import com.typesafe.scalalogging.StrictLogging

import javax.swing.{ JFrame, JPanel, SwingUtilities, Timer }

trait AnimationHelper extends StrictLogging {

  self: JPanel with ActionListener =>

  val timer = new Timer(200, this)
  val animationDuration = 4000
  lazy val animationStartTime = System.currentTimeMillis()
  lazy val animationEndTime = animationStartTime + animationDuration
  var lastCall = System.currentTimeMillis()
  var fraction: Float = 0
  timer.start()

  final override def actionPerformed(e: ActionEvent) {
    val now = System.currentTimeMillis()
    logger.info("Called after " + (now - lastCall))
    if (now > animationEndTime) {
      logger.info("Stopping")
      fraction = 1
      timer.stop()
    } else {
      fraction = 1 - (animationEndTime - now).toFloat / animationDuration
      //      
    }
    repaint()
  }

}
object AppHelper {

  def doCreateAndShowGUI(name: String, panel: JPanel) = SwingUtilities.invokeLater(
    new Runnable() {
      override def run() {
        val f = new JFrame(name)
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE)
        f.setSize(1000, 1000)
        println(panel)
        f.add(panel)
        f.setVisible(true)
        f.setResizable(false)
      }
    })

}
object Zoom2Pages2 extends App {
  AppHelper.doCreateAndShowGUI("FixedHeightPan", new Zoom2Pages2)
}
object Zoom2PagesWithOverlap extends App {
  AppHelper.doCreateAndShowGUI("Zoom 2 Pages with Offset", new ZoomXPages(2))
}
object Zoom4PagesWithOverlap extends App {
  AppHelper.doCreateAndShowGUI("Zoom 4 Pages with Offset", new ZoomXPages(4))
}
object Zoom8PagesWithOverlap extends App {
  AppHelper.doCreateAndShowGUI("Zoom 8 Pages with Offset", new ZoomXPages(8))
}

trait PanelHelper extends JPanel with ActionListener with AnimationHelper {
  def zoomFactor: Int

  final private def background(g: Graphics, width: Int, height: Int) {
    g.fillRect(0, 0, width * zoomFactor, height)
    g.setColor(Color.white)
    Random.setSeed(0)
    for (_ <- 1 to 100 * 100) {
      g.setColor(new Color(Random.nextInt(255), Random.nextInt(255), Random.nextInt(255)))
      g.fillRect(Random.nextInt(width * zoomFactor - 5), Random.nextInt(height - 5), 5, 5)
    }
    // some markers
    for (i <- 1 to 100) {
      g.drawString(s"$i", (width * zoomFactor * i / 100.0).toInt, height / 2)
    }
  }
  final lazy val bufferedImage = {
    val gc = getGraphicsConfiguration
    val image = gc.createCompatibleImage(getWidth() * zoomFactor, getHeight(), Transparency.BITMASK)
    val gImg = image.getGraphics.asInstanceOf[Graphics2D]
    background(gImg, getWidth, getHeight)
    gImg.dispose()
    image
  }
  lazy val pageWidth = getWidth
  lazy val totalWidth = pageWidth * zoomFactor
}

class Zoom2Pages2 extends PanelHelper {
  val zoomFactor = 2

  override def paintComponent(g: Graphics) {
    //    println(fraction)
    val page = max(1, ceil(zoomFactor * fraction).toInt)
    val x = (page - 1) * bufferedImage.getWidth() / zoomFactor
    val w = bufferedImage.getWidth() / zoomFactor
    val pageFraction = (fraction - (page - 1) * ((page - 1).toFloat / zoomFactor)) * zoomFactor
    val translate = (pageFraction * w).toInt
    println(s"fraction $fraction, page $page, pageFraction $pageFraction, translate $translate, x $x, w $w")
    val curImage = bufferedImage.getSubimage(x, 0, w, bufferedImage.getHeight())
    g.drawImage(curImage, 0, 0, null)
    g.fillRect(translate, 0, 5, getHeight)
  }
}

class ZoomXPages(val zoomFactor: Int) extends PanelHelper {
  case class Page(idx: Int, startPos: Int, endPos: Int, startDisp: Int, endDisp: Int) {
    override def toString = s"$idx($startPos/$endPos)($startDisp/$endDisp)"
  }

  private def pageBorders(width: Int, zoomFactor: Int): Seq[Page] = {
    logger.debug(s"pageBorders width=$width, zoomFactor=$zoomFactor")
    val stepSize = (width * 0.8).toInt
    val offset = width - stepSize
    @tailrec
    def go(cur: Int, acc: Seq[Page]): Seq[Page] = {
      // println(s"go $cur, acc $acc")
      val lastBorder = width * zoomFactor - stepSize
      if (cur >= lastBorder) {
        val startDis = if (cur == 0) 0 else width * zoomFactor - width
        acc :+ Page(acc.size, cur, width * zoomFactor, startDis, width * zoomFactor)
      } else {
        val startPos = cur
        val endPos = if (cur == 0) cur + stepSize else cur + stepSize - offset
        val startDis = if (cur == 0) 0 else cur - offset
        val endDis = startDis + width
        go(endPos, acc :+ Page(acc.size, startPos, endPos, startDis, endDis))
      }
    }
    go(0, Seq())
  }

  lazy val pages = {
    val tmp = pageBorders(getWidth(), zoomFactor)
    logger.debug(s"Borders: $tmp")
    tmp
  }

  override def paintComponent(g: Graphics) {
    val pixelFraction = Math.round(totalWidth * fraction)
    val page = pages.find(e => e.startDisp <= pixelFraction && e.endPos >= pixelFraction).get
    val translate = pixelFraction - page.startDisp
    println(s"fraction $fraction, page $page, pageIdx $page.idx, translate $translate")
    val curImage = bufferedImage.getSubimage(page.startDisp, 0, pageWidth, bufferedImage.getHeight())
    g.drawImage(curImage, 0, 0, null)
    g.fillRect(translate, 0, 5, getHeight)
  }
}

