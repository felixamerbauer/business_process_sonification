package swing.animation

import java.awt.{ Graphics, Graphics2D, Transparency }
import java.awt.event.ActionListener

import javax.swing.{ JPanel, SwingUtilities }

object AnimationBufferedImage extends App {
  val doCreateAndShowGUI = new Runnable() {
    def run() { createAndShowGUI("AnimationBufferedImage", new BufferedImagePanel) }
  }
  SwingUtilities.invokeLater(doCreateAndShowGUI)
}

class BufferedImagePanel extends JPanel with ActionListener with AnimationHelper {
  lazy val bufferedImage = {
    val gc = getGraphicsConfiguration
    val image = gc.createCompatibleImage(getWidth(), getHeight(), Transparency.BITMASK)
    val gImg = image.getGraphics.asInstanceOf[Graphics2D]
    background(gImg, getWidth, getHeight)
    gImg.dispose()
    image
  }

  override def paintComponent(g: Graphics) {
    g.drawImage(bufferedImage, 0, 0, null)
    g.fillRect(translate, 0, 5, getHeight)
  }

}
