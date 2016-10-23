package swing.filthyrichclients

import java.awt.{ Color, Graphics }
import java.awt.event.{ ActionEvent, ActionListener }

import javax.swing.{ JButton, JFrame, JPanel, SwingUtilities, Timer }

class AnimationButton(label: String) extends JButton(label) with ActionListener {
  // for later start/stop actions
  val timer = new Timer(30, this)
  // each animation will take 2 seconds
  val animationDuration = 2000
  // start time for each animation
  var animStartTime: Long = _
  // y location of the button
  var translateY = 0

  setOpaque(false)
  addActionListener(this)

  override def paint(g: Graphics) {
    g.translate(0, translateY)
    super.paint(g)
  }

  override def actionPerformed(ae: ActionEvent): Unit = {
    if (ae.getSource() == this) {
      // button click
      if (!timer.isRunning()) {
        animStartTime = System.nanoTime() / 1000000
        this.setText("Stop Animation")
        timer.start()
      } else {
        timer.stop()
        this.setText("Start Animation")
        // reset translation to 0
        translateY = 0
      }
    } else {
      // Timer event
      // calculate the elapsed fraction
      val currentTime = System.nanoTime() / 1000000
      val totalTime = currentTime - animStartTime
      if (totalTime > animationDuration) {
        animStartTime = currentTime
      }
      var fraction = totalTime.toFloat / animationDuration
      fraction = Math.min(1.0f, fraction)
      // This calculation will cause translateY to go from 0 to MAX_Y
      // as the fraction goes from 0 to 1
      if (fraction < .5f) {
        translateY = (AnimationButton.MAX_Y * (2 * fraction)).toInt
      } else {
        translateY = (AnimationButton.MAX_Y * (2 * (1 - fraction))).toInt
      }
      // redisplay our component with the new location
      repaint()
    }
  }
}

object AnimationButton {
  val MAX_Y = 100

  def createAndShowGUI() {
    val f = new JFrame("Moving Button")
    f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE)
    f.setSize(300, 300)
    val checkerboard = new Checkerboard()
    checkerboard.add(new AnimationButton("Start Animation"))
    f.add(checkerboard)
    f.setVisible(true)
  }
}

class Checkerboard extends JPanel {

  import Checkerboard._

  override def paintComponent(g: Graphics) {
    g.setColor(Color.white)
    g.fillRect(0, 0, getWidth, getHeight)
    g.setColor(Color.BLACK)
    for (stripeX <- 0 until getWidth by CHECKER_SIZE) {
      var row = 0
      for (y <- 0 until getHeight by CHECKER_SIZE / 2) {
        row += 1
        val x =
          if (row % 2 == 0) stripeX
          else CHECKER_SIZE / 2 + stripeX
        g.fillRect(x, y, CHECKER_SIZE / 2, CHECKER_SIZE / 2)
      }
    }
  }
}

object Checkerboard {
  val DIVISIONS = 10
  val CHECKER_SIZE = 60
}


object AnimationButtonRunner extends App {
  val doCreateAndShowGUI = new Runnable() {
    override def run() {
      AnimationButton.createAndShowGUI()
    }
  }
  SwingUtilities.invokeLater(doCreateAndShowGUI)
}
