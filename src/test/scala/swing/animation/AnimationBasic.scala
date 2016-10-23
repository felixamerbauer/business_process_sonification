package swing.animation

import java.awt.Graphics
import java.awt.event.ActionListener

import javax.swing.{ JPanel, SwingUtilities }

object AnimationBasic extends App {
  val doCreateAndShowGUI = new Runnable() {
    def run() { createAndShowGUI("AnimationBasic",new BasicPanel) }
  }
  SwingUtilities.invokeLater(doCreateAndShowGUI)
}

class BasicPanel extends JPanel with ActionListener with AnimationHelper {

  override def paintComponent(g: Graphics) {
    background(g, getWidth, getHeight)
    g.fillRect(translate, 0, 5, getHeight)
  }

}
