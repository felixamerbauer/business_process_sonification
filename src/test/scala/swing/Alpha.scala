package swing

import java.awt.{ AlphaComposite, Color, Graphics, Graphics2D }

import javax.swing.{ JFrame, JPanel, SwingUtilities }
import sonification.ui.left.Shapes.dashedStroke

object Alpha extends App {

  val doCreateAndShowGUI = new Runnable() {
    def run() {
      val f = new JFrame("ActivityIcons")
      f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE)
      f.setSize(310, 250)
      //    checkerboard.add(new MovingButton("Start Animation"))
      f.add(new Alpha())
      f.setVisible(true)
      f.setResizable(false)
    }
  }
  SwingUtilities.invokeLater(doCreateAndShowGUI)
}

class Alpha extends JPanel {

  def createGrid(width: Int, height: Int, g: Graphics2D) {
    val oldStroke = g.getStroke
    g.setStroke(dashedStroke)
    // draw vertical lines
    0 to (width - width % 100) by 100 foreach (e => g.drawLine(e, 0, e, height))
    // draw horizontal lines
    0 to (height - height % 100) by 100 foreach (e => g.drawLine(0, e, width, e))
    g.setStroke(oldStroke)
  }

  override def paintComponent(g: Graphics) {
    val g2d = g.asInstanceOf[Graphics2D]

    def setAlpha(value: Float) = g2d.setComposite(
      AlphaComposite.getInstance(AlphaComposite.SRC_OVER, value))

    // Grid
    createGrid(getSize().width, getSize().height, g2d)
    // Shapes
    g.setColor(Color.RED)
    g.fillRect(25, 25, 50, 50)
    setAlpha(0.8f)
    g.fillRect(125, 25, 50, 50)
    setAlpha(0.6f)
    g.fillRect(225, 25, 50, 50)
    setAlpha(0.4f)
    g.fillRect(25, 125, 50, 50)
    setAlpha(0.2f)
    g.fillRect(125, 125, 50, 50)
    setAlpha(1.0f)
    g.fillRect(150, 125, 50, 50)
    setAlpha(0.0f)
    g.fillRect(225, 125, 50, 50)
  }

}
