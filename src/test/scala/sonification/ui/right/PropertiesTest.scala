package sonification.ui.right

import javax.swing.{ JFrame, SwingUtilities }
import sonification.controller.Controller

object PropertiesTester extends App {

  val doCreateAndShowGUI = new Runnable() {
    def run() {
      val f = new JFrame("ActivityIcons")
      f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE)
      f.setSize(600, 200)
      //    checkerboard.add(new MovingButton("Start Animation"))
      implicit val ctrl:Controller = null
      f.add(new Properties())
      f.setVisible(true)
      f.setResizable(true)
    }
  }
  SwingUtilities.invokeLater(doCreateAndShowGUI)
}
