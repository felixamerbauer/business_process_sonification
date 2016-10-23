package swing

import java.awt.{ Color, Graphics, Graphics2D }

import javax.swing.{ JFrame, JPanel, SwingUtilities }
import sonification.controller.{ AllShapes, Colors }
import sonification.ui.left.Shapes.createCircle
import sonification.ui.left.XY

object ShapesThesis extends App {

  val doCreateAndShowGUI = new Runnable() {
    def run() {
      val f = new JFrame("ShapesThesis")
      f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE)
      f.setSize(1840, 1350)
      f.add(new ShapesThesis)
      f.setVisible(true)
      f.setResizable(false)
    }
  }
  SwingUtilities.invokeLater(doCreateAndShowGUI)
}

class ShapesThesis extends JPanel {

  override def paintComponent(g: Graphics) {
    implicit val g2d = g.asInstanceOf[Graphics2D]
    val itemSize = 144
    val spacing = 180
    // draw all shapes in all colors
    for {
      (color, x) <- Colors.zipWithIndex
      (shape, y) <- AllShapes.zipWithIndex
      xy = XY(x * spacing + spacing / 2, y * spacing + spacing / 2)
    } shape.draw(xy, color, itemSize)
    // draw dots
    0 until Colors.size foreach { x =>
      val circle = createCircle(XY(x * spacing + spacing / 2, AllShapes.size * spacing + 50), 5)
      g2d.setColor(Color.black)
      g2d.draw(circle)
      g2d.fill(circle)
    }

  }

}
