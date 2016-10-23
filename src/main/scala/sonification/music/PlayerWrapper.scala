package sonification.music

import org.jfugue.player.{ ManagedPlayer, ManagedPlayerListener, Player }

import com.typesafe.scalalogging.StrictLogging

import javax.sound.midi.Sequence

/** handles jfugue playback */
class PlayerWrapper extends StrictLogging {
  logger.debug("PlayerWrapper()")

  // player instance (singleton) so that an already started playback can be stopped again
  private var managedPlayer: ManagedPlayer = _

  def init(sequence: Sequence): Unit = {
    logger.debug("init")
    managedPlayer = new Player().getManagedPlayer
    managedPlayer.start(sequence)
    managedPlayer.pause()
    logger.debug("init finished")
  }

  /**
   * stops the current playback (only if currently playing)
   */
  def finish(): Unit = {
    logger.debug("finish playing")
    if (!managedPlayer.isFinished()) {
      managedPlayer.finish()
    }
  }

  def removeListeners(): Unit = {
    // TODO managedPlayer.removeManagedPlayerListener doesn't work in JFugue 5.0.3 so we need to work around this limitation
    // managedPlayer.removeManagedPlayerListener(e)
    managedPlayer.getManagedPlayerListeners.clear()
  }

  def seek(tick: Long): Unit = {
    logger.debug(s"seek $tick")
    managedPlayer.seek(tick)
  }
  def seek(progress: Float): Unit = {
    logger.debug(s"seek $progress")
    managedPlayer.seek(Math.round(managedPlayer.getTickLength * progress))
  }

  def resume(): Unit = {
    logger.debug("Resume playing")
    managedPlayer.resume()
  }

  def pause(): Unit = {
    logger.debug("Pause playing")
    managedPlayer.pause()
  }

  def addListener(listener: ManagedPlayerListener): Unit = {
    logger.debug(s"addListener $listener")
    managedPlayer.addManagedPlayerListener(listener)
  }

  def progress: Float = {
    val p = managedPlayer.getTickPosition / managedPlayer.getTickLength.toFloat
    //    logger.debug(s"progress length=${managedPlayer.getTickLength}, position=${managedPlayer.getTickPosition}, progress=$p")
    p
  }

}