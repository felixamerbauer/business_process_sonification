package swing.filthyrichclients

import java.awt.event.{ ActionEvent, ActionListener }
import java.lang.System.currentTimeMillis

import com.typesafe.scalalogging.StrictLogging

import javax.swing.Timer

object SwingTimerDemo extends App with StrictLogging {
  var prevTime: Long = 0
  private var startTime: Long = 0
  private final val DELAY: Int = 100
  private final val DURATION: Int = 5 * DELAY
  private final val PROCESSING_TIME: Int = 30
  private final val INITIAL_PROCESSING_TIME: Long = 2 * DELAY
  private var timer: Timer = null

  timer = new Timer(DELAY, new SwingTimerDemo)
  startTime = {
    prevTime = currentTimeMillis
    prevTime
  }
  logger.info("Fixed Delay Times")
  timer.start()
  try {
    Thread.sleep(DURATION * 2)
  } catch {
    case e: Exception =>
  }
  timer = new Timer(DELAY, new SwingTimerDemo)
  startTime = {
    prevTime = currentTimeMillis
    prevTime
  }
  timer.setCoalesce(false)
  logger.info("\nFixed Rate Times")
  timer.start()

}

class SwingTimerDemo extends ActionListener with StrictLogging {
  import SwingTimerDemo._
  private var firstTime: Boolean = true

  override def actionPerformed(ae: ActionEvent) {
    val nowTime = currentTimeMillis
    val elapsedTime = nowTime - prevTime
    val totalTime = nowTime - startTime
    logger.info("Elapsed time = " + elapsedTime)
    if (totalTime > DURATION) {
      timer.stop()
    }
    prevTime = nowTime
    try {
      if (firstTime) {
        Thread.sleep(INITIAL_PROCESSING_TIME)
        firstTime = false
      } else {
        Thread.sleep(PROCESSING_TIME)
      }
    } catch {
      case e: Exception => {
      }
    }
  }
}