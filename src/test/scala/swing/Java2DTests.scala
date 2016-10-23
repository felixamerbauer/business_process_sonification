package swing

import java.awt.{ Graphics, Graphics2D }
import java.awt.geom.Ellipse2D

import javax.swing.{ JPanel, SwingUtilities }
import javax.swing.JFrame
import javax.swing.JFrame.EXIT_ON_CLOSE

object Java2DTests extends App {
  class MyPanel extends JPanel {
    override def paintComponent(g: Graphics) {
      super.paintComponent(g)
      val g2d = g.asInstanceOf[Graphics2D]
      val circle = new Ellipse2D.Double(50, 50, 20, 20)
      g2d.fill(circle)
    }
  }

  class MyFrame extends JFrame {

    setTitle("Java2DTests")
    setSize(800, 800)
    setLocationRelativeTo(null)
    setDefaultCloseOperation(EXIT_ON_CLOSE)
    setContentPane(new MyPanel)
  }

  SwingUtilities.invokeLater(new Runnable() {
    override def run() {
      val ex = new MyFrame
      ex.setVisible(true)
    }
  })

}