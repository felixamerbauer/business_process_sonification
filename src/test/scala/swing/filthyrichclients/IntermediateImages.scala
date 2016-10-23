package swing.filthyrichclients

import java.awt.{ BasicStroke, Color, Graphics, Graphics2D, RenderingHints, Transparency }
import java.awt.image.BufferedImage

import com.typesafe.scalalogging.StrictLogging

import javax.imageio.ImageIO
import javax.swing.{ JComponent, JFrame, SwingUtilities }

object IntermediateImages extends App with StrictLogging {

  private val SCALE_X = 20
  private val SMILEY_X = 200
  private val DIRECT_Y = 10
  private val INTERMEDIATE_Y = 260
  private val SMILEY_SIZE = 100

  private var picture: BufferedImage = null

  private val SCALE_FACTOR = .1

  private def createAndShowGUI() {
    val f = new JFrame("IntermediateImages")
    f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE)
    f.setSize(360, 540)
    f.add(new IntermediateImages())
    f.setVisible(true)
  }

  val doCreateAndShowGUI = new Runnable() {
    def run() {
      createAndShowGUI()
    }
  }
  SwingUtilities.invokeLater(doCreateAndShowGUI)
}

class IntermediateImages extends JComponent with StrictLogging {
  import IntermediateImages._

  private var scaledImage: BufferedImage = null
  private var smileyImage: BufferedImage = null
  private var scaleW: Int = _
  private var scaleH: Int = _

  try {
    val url = getClass.getResource("BB.jpg")
    picture = ImageIO.read(url)
    scaleW = (SCALE_FACTOR * picture.getWidth).toInt
    scaleH = (SCALE_FACTOR * picture.getHeight).toInt
  } catch {
    case e: Exception => {
      logger.info("Problem reading image file: " + e)
      System.exit(0)
    }
  }

  private def drawScaled(g: Graphics) {
    var startTime: Long = 0l
    var endTime: Long = 0l
    var totalTime: Long = 0l
    g.asInstanceOf[Graphics2D].setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR)
    startTime = System.nanoTime()
    for (i <- 0 until 100) {
      g.drawImage(picture, SCALE_X, DIRECT_Y, scaleW, scaleH, null)
    }
    endTime = System.nanoTime()
    totalTime = (endTime - startTime) / 1000000
    g.setColor(Color.BLACK)
    g.drawString("Direct: " + (totalTime.toFloat / 100) + " ms", SCALE_X, DIRECT_Y + scaleH + 20)
    logger.info("scaled: " + totalTime)
    if (scaledImage == null || scaledImage.getWidth != scaleW || scaledImage.getHeight != scaleH) {
      val gc = getGraphicsConfiguration
      scaledImage = gc.createCompatibleImage(scaleW, scaleH)
      val gImg = scaledImage.getGraphics
      gImg.asInstanceOf[Graphics2D].setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR)
      gImg.drawImage(picture, 0, 0, scaleW, scaleH, null)
    }
    startTime = System.nanoTime()
    for (i <- 0 until 100) {
      g.drawImage(scaledImage, SCALE_X, INTERMEDIATE_Y, null)
    }
    endTime = System.nanoTime()
    totalTime = (endTime - startTime) / 1000000
    g.drawString("Intermediate: " + (totalTime.toFloat / 100) + " ms", SCALE_X, INTERMEDIATE_Y + scaleH + 20)
    logger.info("Intermediate scaled: " + totalTime)
  }

  private def renderSmiley(g: Graphics, x: Int, y: Int) {
    val g2d = g.create().asInstanceOf[Graphics2D]
    g2d.setColor(Color.yellow)
    g2d.fillOval(x, y, SMILEY_SIZE, SMILEY_SIZE)
    g2d.setColor(Color.black)
    g2d.fillOval(x + 30, y + 30, 8, 8)
    g2d.fillOval(x + 62, y + 30, 8, 8)
    g2d.drawOval(x, y, SMILEY_SIZE, SMILEY_SIZE)
    g2d.setStroke(new BasicStroke(3.0f))
    g2d.drawArc(x + 20, y + 20, 60, 60, 190, 160)
    g2d.dispose()
  }

  private def drawSmiley(g: Graphics) {
    var startTime: Long = 0l
    var endTime: Long = 0l
    var totalTime: Long = 0l
    startTime = System.nanoTime()
    for (i <- 0 until 100) {
      renderSmiley(g, SMILEY_X, DIRECT_Y)
    }
    endTime = System.nanoTime()
    totalTime = (endTime - startTime) / 1000000
    g.setColor(Color.BLACK)
    g.drawString("Direct: " + (totalTime.toFloat / 100) + " ms", SMILEY_X, DIRECT_Y + SMILEY_SIZE + 20)
    logger.info("Direct: " + totalTime)
    if (smileyImage == null) {
      val gc = getGraphicsConfiguration
      smileyImage = gc.createCompatibleImage(SMILEY_SIZE + 1, SMILEY_SIZE + 1, Transparency.BITMASK)
      val gImg = smileyImage.getGraphics.asInstanceOf[Graphics2D]
      renderSmiley(gImg, 0, 0)
      gImg.dispose()
    }
    startTime = System.nanoTime()
    for (i <- 0 until 100) {
      g.drawImage(smileyImage, SMILEY_X, INTERMEDIATE_Y, null)
    }
    endTime = System.nanoTime()
    totalTime = (endTime - startTime) / 1000000
    g.drawString("Intermediate: " + (totalTime.toFloat / 100) + " ms", SMILEY_X, INTERMEDIATE_Y + SMILEY_SIZE + 20)
    logger.info("intermediate smiley: " + totalTime)
  }

  protected override def paintComponent(g: Graphics) {
    g.setColor(Color.WHITE)
    g.fillRect(0, 0, getWidth, getHeight)
    drawScaled(g)
    drawSmiley(g)
  }
}
