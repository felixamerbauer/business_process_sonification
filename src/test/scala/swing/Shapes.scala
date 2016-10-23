package swing

import java.awt.{ Graphics, Graphics2D }

import javax.swing.{ JFrame, JPanel, SwingUtilities }
import sonification.ui.left.Shapes.{ createCircle, createCross, createRectangle, createRhombus, createStar, dashedStroke }
import sonification.ui.left.XY

object Shapes extends App {

  val doCreateAndShowGUI = new Runnable() {
    def run() {
      val f = new JFrame("ActivityIcons")
      f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE)
      f.setSize(310, 250)
      //    checkerboard.add(new MovingButton("Start Animation"))
      f.add(new Shapes())
      f.setVisible(true)
      f.setResizable(false)
    }
  }
  SwingUtilities.invokeLater(doCreateAndShowGUI)
}

class Shapes extends JPanel {

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
    // Grid
    createGrid(getSize().width, getSize().height, g2d)
    // Shapes
    val circle = createCircle(XY(50, 50), 50)
    g2d.draw(circle)
    val rectangle = createRectangle(XY(150, 50), 100)
    g2d.draw(rectangle)
//    val triangle = createTriangle(XY(150, 150), 100)
//    g2d.draw(triangle)
    val cross = createCross(XY(50, 150), 100)
    g2d.draw(cross)
    val rhombus = createRhombus(XY(250, 150), 100)
    g2d.draw(rhombus)
    val star = createStar(XY(250,50),100)
    g2d.draw(star)
  }

}
