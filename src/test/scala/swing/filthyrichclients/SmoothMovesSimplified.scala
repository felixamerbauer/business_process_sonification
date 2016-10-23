package swing.filthyrichclients

import java.awt.{ Color, Graphics, Transparency }
import java.awt.event.{ ActionEvent, ActionListener }
import java.awt.image.BufferedImage

import javax.swing.{ JComponent, JFrame, SwingUtilities, Timer }

object SmoothMovesSimplified extends App {

  var imageW: Int = 100
  var imageH: Int = 150
  var moveMinX: Int = 150
  var moveMaxX: Int = 350

  val CYCLE_TIME = 2000

  private def createAndShowGUI() {
    val f = new JFrame("Smooth Moves")
    f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE)
    f.setSize(moveMaxX + imageW + 50, 300)
    val component = new SmoothMovesSimplified()
    f.add(component)
    f.setVisible(true)
  }

  val doCreateAndShowGUI = new Runnable() {
    override def run() { createAndShowGUI() }
  }
  SwingUtilities.invokeLater(doCreateAndShowGUI)
}

class SmoothMovesSimplified extends JComponent with ActionListener {
  import SmoothMovesSimplified._

  var image: BufferedImage = null
  var moveX: Int = moveMinX
  var moveY: Int = 50
  var opacity: Float = 0.0f
  var prevMoveX: Array[Int] = _
  var prevMoveY: Array[Int] = _
  var trailOpacity: Array[Float] = _
  var currentResolution: Int = 50
  var timer: Timer = null
  var cycleStart: Long = System.nanoTime() / 1000000

  startTimer(currentResolution)

  def createAnimationImage() {
    val gc = getGraphicsConfiguration
    image = gc.createCompatibleImage(imageW, imageH, Transparency.TRANSLUCENT)
    val gImg = image.createGraphics()
    var graphicsColor: Color = null
    graphicsColor = Color.BLACK
    gImg.setColor(graphicsColor)
    gImg.fillRect(0, 0, imageW, imageH)
    gImg.dispose()
  }

  override def paintComponent(g: Graphics) {
    if (image == null) {
      createAnimationImage()
    }
    g.setColor(Color.WHITE)
    g.fillRect(0, 0, getWidth, getHeight)
    g.drawImage(image, moveX, moveY, null)
  }

  def actionPerformed(ae: ActionEvent) {
    val currentTime = System.nanoTime() / 1000000
    val totalTime = currentTime - cycleStart
    if (totalTime > CYCLE_TIME) {
      cycleStart = currentTime
    }
    var fraction = totalTime.toFloat / CYCLE_TIME
    fraction = Math.min(1.0f, fraction)
    fraction = 1 - Math.abs(1 - (2 * fraction))
    animate(fraction)
  }

  def animate(fraction: Float) {
    var animationFactor = 0.0f
    animationFactor = fraction
    animationFactor = Math.min(animationFactor, 1.0f)
    animationFactor = Math.max(animationFactor, 0.0f)
    opacity = animationFactor
    moveX = moveMinX + (.5f + animationFactor * (moveMaxX - moveMinX).toFloat).toInt
    repaint()
  }

  private def startTimer(resolution: Int) {
    if (timer != null) {
      timer.stop()
      timer.setDelay(resolution)
    } else {
      timer = new Timer(resolution, this)
    }
    timer.start()
  }
}
