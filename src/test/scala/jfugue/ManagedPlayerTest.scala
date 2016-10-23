package jfugue

import java.io.{ ByteArrayInputStream, ByteArrayOutputStream }
import java.util.concurrent.TimeUnit.SECONDS

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits
import scala.concurrent.Future
import scala.concurrent.duration.DurationInt
import scala.util.Random

import org.jfugue.pattern.Pattern
import org.jfugue.player.{ ManagedPlayer, ManagedPlayerListener, Player }

import com.typesafe.scalalogging.StrictLogging

import javax.sound.midi.Sequence
import sonification.music.Midi2WavRenderS
import sun.audio.{ AudioPlayer, AudioStream }

class MyManagedPlayerLister extends ManagedPlayerListener with StrictLogging {
  private val id = Random.alphanumeric.take(5).mkString
  override def onStarted(seq: Sequence) {
    logger.debug(s"$id onStarted $seq")
  }
  override def onFinished {
    logger.debug(s"$id onFinished")
  }
  override def onPaused {
    logger.debug(s"$id onPaused")
  }
  override def onResumed {
    logger.debug(s"$id onResumed")
  }
  override def onSeek(tick: Long) {
    logger.debug(s"$id onSeek $tick")
  }
  override def onReset {
    logger.debug("onReset")
  }
}

object ManagedPlayerTest extends App with StrictLogging {
  import scala.concurrent.ExecutionContext.Implicits.global
  val ms = "c d e f g a b"

  val sequence = new Player().getSequence(new Pattern(ms))

  def play() {
    val mp = new ManagedPlayer()
    mp.addManagedPlayerListener(new MyManagedPlayerLister())
    mp.start(sequence)
  }

  logger.debug("Starting p1")
  val p1 = Future { play }
  SECONDS.sleep(1)
  val wav = {
    val seq = new Player().getSequence("c")
    val baos = new ByteArrayOutputStream()
    Midi2WavRenderS.render(null, seq, baos)
    val bytes = baos.toByteArray()
    val is = new ByteArrayInputStream(bytes)
    val audioStream = new AudioStream(is)
    AudioPlayer.player.start(audioStream);
  }
  //  logger.debug("Starting p2")
  //  val p2 = Future { play }
  //  SECONDS.sleep(1)
  logger.debug("Starting p3")
  //  val p3 = Future { play }
  val combined = for {
    _ <- p1
    //    _ <- p2
    //    _ <- p3
  } yield ()

  Await.result(combined, 1 hour)
  SECONDS.sleep(5)
}