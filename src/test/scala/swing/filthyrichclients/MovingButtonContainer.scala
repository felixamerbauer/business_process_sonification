package swing.filthyrichclients

import java.awt.{ Color, Graphics }
import java.awt.event.{ ActionEvent, ActionListener }

import javax.swing.{ JButton, JComponent, JFrame, SwingUtilities, Timer }

object MovingButtonContainer extends App {

  val MAX_Y = 100
  private val DIVISIONS = 10
  val CHECKER_SIZE = 60

  private def createAndShowGUI() {
    val f = new JFrame("Moving Button Container")
    f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE)
    f.setSize(300, 300)
    val buttonContainer = new MovingButtonContainer()
    f.add(buttonContainer)
    f.setVisible(true)
  }

  val doCreateAndShowGUI = new Runnable() {

    def run() {
      createAndShowGUI()
    }
  }
  SwingUtilities.invokeLater(doCreateAndShowGUI)
}

class MovingButtonContainer extends JComponent with ActionListener {
  import MovingButtonContainer._

  var timer: Timer = new Timer(30, this)
  var animationDuration: Int = 2000
  var animStartTime: Long = _
  var translateY: Int = 0
  var button: JButton = new JButton("Start Animation")

  setLayout(new java.awt.FlowLayout())

  button.setOpaque(false)
  button.addActionListener(this)

  add(button)

  def actionPerformed(ae: ActionEvent) {
    if (ae.getSource == button) {
      if (!timer.isRunning) {
        animStartTime = System.nanoTime() / 1000000
        button.setText("Stop Animation")
        timer.start()
      } else {
        timer.stop()
        button.setText("Start Animation")
        translateY = 0
        repaint()
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

  override def paint(g: Graphics) {
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
    g.translate(0, translateY)
    super.paint(g)
  }
}
