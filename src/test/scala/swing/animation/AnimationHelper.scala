package swing.animation

import java.awt.event.{ ActionEvent, ActionListener }

import com.typesafe.scalalogging.StrictLogging

import javax.swing.{ JPanel, Timer }

trait AnimationHelper extends StrictLogging {

  self: JPanel with ActionListener =>

  val timer = new Timer(30, this)
  val animationDuration = 2000
  lazy val animationStartTime = System.currentTimeMillis()
  lazy val animationEndTime = animationStartTime + animationDuration
  var lastCall = System.currentTimeMillis()
  var translate: Int = 0
  timer.start()

  final override def actionPerformed(e: ActionEvent) {
    val now = System.currentTimeMillis()
    logger.info("Called after " + (now - lastCall))
    if (now > animationEndTime) {
      logger.info("Stopping")
      timer.stop()
    } else {
      val fraction = 1 - (animationEndTime - now).toFloat / animationDuration
      translate = Math.round(fraction * getWidth())
      repaint()
    }
  }

}