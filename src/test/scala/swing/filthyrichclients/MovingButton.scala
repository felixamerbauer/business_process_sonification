package swing.filthyrichclients

import java.awt.{ Color, Graphics }
import java.awt.event.{ ActionEvent, ActionListener }

import javax.swing.{ JButton, JFrame, JPanel, SwingUtilities, Timer }

object MovingButton extends App {

  val MAX_Y = 100
  val doCreateAndShowGUI = new Runnable() {
    def run() {
      createAndShowGUI()
    }
  }
  SwingUtilities.invokeLater(doCreateAndShowGUI)

  private def createAndShowGUI() {
    val f = new JFrame("Moving Button")
    f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE)
    f.setSize(300, 300)
    val checkerboard = new Checkerboard()
    checkerboard.add(new MovingButton("Start Animation"))
    f.add(checkerboard)
    f.setVisible(true)
  }

  object Checkerboard {
    private val DIVISIONS = 10
    val CHECKER_SIZE = 60
  }

  private class Checkerboard extends JPanel {
    import Checkerboard._

    override def paintComponent(g: Graphics) {
      g.setColor(Color.white)
      g.fillRect(0, 0, getWidth, getHeight)
      g.setColor(Color.BLACK)
      var stripeX = 0
      while (stripeX < getWidth) {
        var y = 0
        var row = 0
        while (y < getHeight) {
          val x = if ((row % 2 == 0)) stripeX else (stripeX + CHECKER_SIZE / 2)
          g.fillRect(x, y, CHECKER_SIZE / 2, CHECKER_SIZE / 2)
          y += CHECKER_SIZE / 2
          row
        }
        stripeX += CHECKER_SIZE
      }
    }
  }

}


class MovingButton(label: String) extends JButton(label) with ActionListener {
  import MovingButton._

  var timer: Timer = new Timer(30, this)
  var animationDuration: Int = 2000
  var animStartTime: Long = _
  var translateY: Int = 0

  setOpaque(false)
  addActionListener(this)

  override def paint(g: Graphics) {
    g.translate(0, translateY)
    super.paint(g)
  }

  def actionPerformed(ae: ActionEvent) {
    if (ae.getSource == this) {
      if (!timer.isRunning) {
        animStartTime = System.nanoTime() / 1000000
        this.setText("Stop Animation")
        timer.start()
      } else {
        timer.stop()
        this.setText("Start Animation")
        translateY = 0
      }
    } else {
      val currentTime = System.nanoTime() / 1000000
      val totalTime = currentTime - animStartTime
      if (totalTime > animationDuration) {
        animStartTime = currentTime
      }
      var fraction = totalTime.toFloat / animationDuration
      fraction = Math.min(1.0f, fraction)
      translateY = if (fraction < .5f) (MAX_Y * (2 * fraction)).toInt else (MAX_Y * (2 * (1 - fraction))).toInt
      repaint()
    }
  }
}
