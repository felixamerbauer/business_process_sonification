package sonification.ui.left

import java.awt.{ Color, Graphics, Graphics2D, GraphicsConfiguration, Transparency }
import java.awt.event.{ MouseEvent, MouseListener }
import java.awt.image.BufferedImage
import com.typesafe.scalalogging.StrictLogging
import javax.swing.{ JPanel, SwingUtilities }
import sonification.Metrics.{ TV_calculateBufferedImage, TV_draw, TV_findItem, TV_paintComponent, t }
import sonification.controller.{ Controller, FitPage, Pause, Play, VisualizationItem }
import sonification.ui.SwingHelper
import sonification.ui.left.Shapes.{ createCircle, createGrid, dashedStroke }
import sonification.ui.left.VisusalizationCalculator.ItemSizeMain
import java.awt.image.DataBuffer

object Visualization extends StrictLogging {
  // TODO replace with something OK
  var dirty = true

  def calculateBufferedImage(ctrl: Controller, gc: GraphicsConfiguration, width: Int, height: Int, itemSize: Int): (Seq[VisualizationItem], BufferedImage) = t(TV_calculateBufferedImage) {
    logger.debug(s"calculateBufferedImage width=$width, height=$height, itemSize=$itemSize")
    def calculateBackground(g: Graphics): Seq[VisualizationItem] = {
      implicit val g2d = g.asInstanceOf[Graphics2D]

      // draw grid
      val lines = createGrid(width, height, ctrl.traces.log.size, itemSize)
      val oldStroke = g2d.getStroke
      val oldColor = g2d.getColor()
      g2d.setStroke(dashedStroke)
      g2d.setColor(Color.white)
      lines foreach g2d.draw
      g2d.setStroke(oldStroke)
      g2d.setColor(oldColor)

      // draw data
      val items = SwingHelper.doBackgroundWorkWithWaitCursor(ctrl.view, VisusalizationCalculator.calculateVisualization(ctrl.traces.log, ctrl.settings, width, height, itemSize))
      items.foreach(e => draw(e, itemSize))
      items
    }
    val image = gc.createCompatibleImage(width, height, Transparency.BITMASK)
    val gImg = image.getGraphics.asInstanceOf[Graphics2D]
    val buff = image.getRaster.getDataBuffer
    val mb = buff.getSize * DataBuffer.getDataTypeSize(buff.getDataType / 8) / 1024 / 1024
    println(s"image size for width $width x $height -> $mb MB")
    val items = calculateBackground(gImg)
    gImg.dispose()
    (items, image)
  }

  def draw(item: VisualizationItem, itemSize: Int)(implicit g2d: Graphics2D): Unit = t(TV_draw) {
    if (item.shape != null && item.color != null && item.shape.enabled && item.color.enabled) {
      item.shape.value.draw(item.xy, item.color.value, itemSize)
    } else {
      val circle = createCircle(item.xy, itemSize / 4)
      g2d.setColor(Color.black)
      g2d.draw(circle)
      g2d.fill(circle)
    }
  }
}

class Visualization(ctrl: Controller) extends JPanel with MouseListener with StrictLogging {

  // environment variables
  private var curWidth: Int = _
  private var curHeight: Int = _
  // buffer
  private var bufferedImage: BufferedImage = _
  private var items: Seq[VisualizationItem] = Seq()

  private val ProgressIndicatorWidth = 4
  // to keep it simple
  assert(ProgressIndicatorWidth % 2 == 0)

  // drawing instructions for visualization
  override def paintComponent(g: Graphics): Unit = t(TV_paintComponent) {
    super.paintComponent(g)
    val zoomFactor = ctrl.settings.zoomLevel.zoomFactor
    if (curWidth != getWidth || curHeight != getHeight || Visualization.dirty) {
      logger.debug(s"dirty $curWidth -> $getWidth, $curHeight -> $getHeight, settings ${Visualization.dirty}")
      // save the new factors influencing the rendering of the image
      curWidth = getWidth
      curHeight = getHeight
      val tmp = Visualization.calculateBufferedImage(ctrl, getGraphicsConfiguration(), curWidth * zoomFactor, curHeight, ItemSizeMain)
      items = tmp._1
      bufferedImage = tmp._2
      Visualization.dirty = false
    }

    // if FitPage start at the beginning
    val startOverview = if (ctrl.settings.zoomLevel == FitPage) 0f
    else ctrl.view.MainSplitPane.left.visualizationOverview.start
    val startPixel = Math.round(getWidth * zoomFactor * startOverview)
    // logger.debug(s"startPixel=$startPixel curWidth=$curWidth curHeight=$curHeight bufferedImage.getWidth=${bufferedImage.getWidth}")
    val curImage = bufferedImage.getSubimage(Math.min(startPixel,bufferedImage.getWidth-curWidth), 0, curWidth, curHeight)
    g.drawImage(curImage, 0, 0, null)
    val end = startOverview + 1 / zoomFactor.toFloat

    def calcTranslate: Int = {
      // playback may already be > 1 because the last event(s) started at 1.0 is still playing
      // if we are on the beginning (overview) add half the item size as offset
      val plus = if (startOverview == 0) ItemSizeMain / 2 else 0
      val minus =
        // single page (-> no zoom and no panning)
        if (zoomFactor == 1) {
          ItemSizeMain
        } // multipage and not on the last page
        else if (zoomFactor > 1 && startOverview < (1 - 1f / zoomFactor)) {
          0
        } else {
          ItemSizeMain / 2
        }
      Math.round((ctrl.fraction - startOverview) * zoomFactor * (curWidth - minus)) + plus - ProgressIndicatorWidth / 2
    }

    val translate =
      // currently playing/pausing so we should be able to show the progress indicator
      if (ctrl.state == Play || ctrl.state == Pause) {
        Some(calcTranslate)
      } // show it only NOT if it is outside the current page (maybe we are panning at the moment)
      else if (ctrl.fraction >= startOverview && ctrl.fraction <= end) {
        Some(calcTranslate)
      } else None

    translate foreach (g.fillRect(_, 0, ProgressIndicatorWidth, curHeight))

    //    logger.debug(s"fraction=${ctrl.fraction}, startOverview=$startOverview, startPixel=$startPixel, end=$end, translate=$translate")
  }

  private def findItem(x: Int, y: Int): Option[VisualizationItem] = t(TV_findItem) {
    // TODO consider spacing on left and right
    if (items.nonEmpty) Some {
      val item = items.minBy { e => Math.abs(e.xy.x - x) + Math.abs(e.xy.y - y) }
      logger.debug(s"findItem x=$x y=$y item=$item")
      item
    }
    else None
  }

  addMouseListener(this)

  override def mouseClicked(e: MouseEvent) {
    // calculate the current page
    val startOverview = ctrl.view.MainSplitPane.left.visualizationOverview.start

    if (SwingUtilities.isLeftMouseButton(e)) {
      val curPage = startOverview * ctrl.settings.zoomLevel.zoomFactor
      val offset = Math.round(curPage * curWidth).toInt
      logger.debug(s"startOverview=$startOverview, curPage=$curPage, offset=$offset")
      findItem(e.getX + offset, e.getY) foreach ctrl.visualizationItemSelected
    } else if (SwingUtilities.isRightMouseButton(e)) {
      // calculate the fraction on the current page
      val add = (e.getX - ItemSizeMain / 2).toDouble / curWidth / ctrl.settings.zoomLevel.zoomFactor
      val jumpTo = startOverview + add
      logger.debug(s"right x=${e.getX}, add=$add, jumpTo=$jumpTo, startOverview=$startOverview")
      ctrl.jumpTo(jumpTo)
    }
  }

  override def mousePressed(e: MouseEvent) = ()

  override def mouseReleased(e: MouseEvent) = ()

  override def mouseEntered(e: MouseEvent) = ()

  override def mouseExited(e: MouseEvent) = ()
}
