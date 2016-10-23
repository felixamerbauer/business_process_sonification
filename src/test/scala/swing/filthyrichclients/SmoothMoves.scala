package swing.filthyrichclients

import java.awt.{ AlphaComposite, Color, Graphics, Graphics2D, Transparency }
import java.awt.event.{ ActionEvent, ActionListener, KeyEvent, KeyListener }
import java.awt.image.BufferedImage

import com.typesafe.scalalogging.StrictLogging

import javax.imageio.ImageIO
import javax.swing.{ JComponent, JFrame, SwingUtilities, Timer }

object SmoothMoves extends App with StrictLogging {

  var imageW: Int = 100
  var imageH: Int = 150
  var moveMinX: Int = 150
  var moveMaxX: Int = 350

  val CYCLE_TIME = 2000

  private def createAndShowGUI() {
    val f = new JFrame("Smooth Moves")
    f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE)
    f.setSize(moveMaxX + imageW + 50, 300)
    val component = new SmoothMoves()
    f.add(component)
    f.setVisible(true)
    f.addKeyListener(component)
  }

  val doCreateAndShowGUI = new Runnable() {

    def run() {
      createAndShowGUI()
    }
  }
  SwingUtilities.invokeLater(doCreateAndShowGUI)
}

class SmoothMoves extends JComponent with ActionListener with KeyListener with StrictLogging {
  import SmoothMoves._

  var image: BufferedImage = null
  var fadeX: Int = 50
  var fadeY: Int = 50
  var moveX: Int = moveMinX
  var moveY: Int = 50
  var opacity: Float = 0.0f
  var useImage: Boolean = false
  var useAA: Boolean = false
  var motionBlur: Boolean = false
  var alterColor: Boolean = false
  var linear: Boolean = true
  var blurSize: Int = 5
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
    if (useImage) {
      val imagePath = "images/duke.gif"
      try {
        val url = getClass.getResource(imagePath)
        val originalImage = ImageIO.read(url)
        gImg.drawImage(originalImage, 0, 0, imageW, imageH, null)
      } catch {
        case e: Exception =>
          logger.info(s"couldn't load image $imagePath")
          e.printStackTrace()
      }
    } else {
      var graphicsColor: Color = null
      graphicsColor = if (alterColor) Color.LIGHT_GRAY else Color.BLACK
      gImg.setColor(graphicsColor)
      gImg.fillRect(0, 0, imageW, imageH)
      if (useAA) {
        gImg.setComposite(AlphaComposite.Src)
        val red = graphicsColor.getRed
        val green = graphicsColor.getRed
        val blue = graphicsColor.getRed
        gImg.setColor(new Color(red, green, blue, 50))
        gImg.drawRect(0, 0, imageW - 1, imageH - 1)
        gImg.setColor(new Color(red, green, blue, 100))
        gImg.drawRect(1, 1, imageW - 3, imageH - 3)
        gImg.setColor(new Color(red, green, blue, 150))
        gImg.drawRect(2, 2, imageW - 5, imageH - 5)
        gImg.setColor(new Color(red, green, blue, 200))
        gImg.drawRect(3, 3, imageW - 7, imageH - 7)
        gImg.setColor(new Color(red, green, blue, 225))
        gImg.drawRect(4, 4, imageW - 9, imageH - 9)
      }
    }
    gImg.dispose()
  }

  override def paintComponent(g: Graphics) {
    if (image == null) {
      createAnimationImage()
    }
    g.setColor(Color.WHITE)
    g.fillRect(0, 0, getWidth, getHeight)
    val gFade = g.create().asInstanceOf[Graphics2D]
    gFade.setComposite(AlphaComposite.SrcOver.derive(opacity))
    gFade.drawImage(image, fadeX, fadeY, null)
    gFade.dispose()
    if (motionBlur) {
      if (prevMoveX == null) {
        prevMoveX = Array.ofDim[Int](blurSize)
        prevMoveY = Array.ofDim[Int](blurSize)
        trailOpacity = Array.ofDim[Float](blurSize)
        val incrementalFactor = .2f / (blurSize + 1)
        for (i <- 0 until blurSize) {
          prevMoveX(i) = -1
          prevMoveY(i) = -1
          trailOpacity(i) = (.2f - incrementalFactor) - i * incrementalFactor
        }
      } else {
        val gTrail = g.create().asInstanceOf[Graphics2D]
        for (i <- 0 until blurSize if prevMoveX(i) >= 0) {
          gTrail.setComposite(AlphaComposite.SrcOver.derive(trailOpacity(i)))
          gTrail.drawImage(image, prevMoveX(i), prevMoveY(i), null)
        }
        gTrail.dispose()
      }
    }
    g.drawImage(image, moveX, moveY, null)
    if (motionBlur) {
      var i = blurSize - 1
      while (i > 0) {
        prevMoveX(i) = prevMoveX(i - 1)
        prevMoveY(i) = prevMoveY(i - 1)
        i
      }
      prevMoveX(0) = moveX
      prevMoveY(0) = moveY
    }
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
    var animationFactor: Float = 0.0f
    animationFactor = if (linear) fraction else Math.sin(fraction * Math.PI.toFloat / 2).toFloat
    animationFactor = Math.min(animationFactor, 1.0f)
    animationFactor = Math.max(animationFactor, 0.0f)
    opacity = animationFactor
    moveX = moveMinX + (.5f + animationFactor * (moveMaxX - moveMinX).toFloat).toInt
    repaint()
  }

  private def changeResolution(faster: Boolean) {
    if (faster) {
      currentResolution -= 5
    } else {
      currentResolution += 5
    }
    currentResolution = Math.max(currentResolution, 0)
    currentResolution = Math.min(currentResolution, 500)
    startTimer(currentResolution)
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

  def keyPressed(ke: KeyEvent) {
    val keyCode = ke.getKeyCode
    if (keyCode == KeyEvent.VK_B) {
      motionBlur = !motionBlur
    } else if (keyCode == KeyEvent.VK_A) {
      useAA = !useAA
      createAnimationImage()
    } else if (keyCode == KeyEvent.VK_C) {
      alterColor = !alterColor
      createAnimationImage()
    } else if (keyCode == KeyEvent.VK_I) {
      useImage = !useImage
      createAnimationImage()
    } else if (keyCode == KeyEvent.VK_UP) {
      changeResolution(true)
    } else if (keyCode == KeyEvent.VK_DOWN) {
      changeResolution(false)
    } else if (keyCode == KeyEvent.VK_L) {
      linear = !linear
    } else if (keyCode >= KeyEvent.VK_1 && keyCode <= KeyEvent.VK_9) {
      blurSize = keyCode - KeyEvent.VK_0
      prevMoveX = null
      prevMoveY = null
    }
  }

  def keyReleased(ke: KeyEvent) {
  }

  def keyTyped(ke: KeyEvent) {
  }
}
