package sonification.music

import java.io.File
import java.net.URL
import java.util.Locale
import javax.sound.midi.{ MidiDevice, MidiSystem, ShortMessage }
import javax.sound.midi.ShortMessage.{ NOTE_OFF, NOTE_ON }
import javax.sound.midi.Synthesizer
import javax.swing.JOptionPane
import javax.sound.midi.MidiDevice.Info
import org.jfugue.player.Player
import org.jfugue.player.SequencerManager
import org.jfugue.player.SynthesizerManager

object SoundbankSf2Tester extends App {
  def infoString(info: Info) =
    s"""${info.getName}
    |${info.getDescription}
    |${info.getVendor}
    |${info.getVersion}
    """.stripMargin

  val home = System.getProperty("user.home")
  println(s"home $home")

  val synth = MidiSystem.getSynthesizer
  synth.open()

  val sbDefault = synth.getDefaultSoundbank
  println(s"sbDefault $sbDefault")
  val basicInfo = synth.getDeviceInfo
  println(s"deluxeInfo ${infoString(basicInfo)}")
  new Player().play("c d e f g a b")
  //  MinimalMidiPlayer.play(synth)
  synth.unloadAllInstruments(sbDefault)

  // path to soundbank
  val file = new File(s"$home/FluidR3_GM.sf2")
  val sbDeluxe = MidiSystem.getSoundbank(file)
  println(s"sbDeluxe $sbDeluxe")
  synth.loadAllInstruments(sbDeluxe)
  val deluxeInfo = synth.getDeviceInfo
  println(s"deluxeInfo ${infoString(deluxeInfo)}")
  // change the JFugue synthesizer
  val customJFugue = SynthesizerManager.getInstance.setSynthesizer(synth)
  // connect the JFugue sequencer to the new synthesizer
  SequencerManager.getInstance.connectSequencerToSynthesizer()
  // gets played with the new synthesizer
  new Player().play("c d e f g a b")

  //  MinimalMidiPlayer.play(synth)
}

object MinimalMidiPlayer {
  def play(synth: Synthesizer) {
    // Use Gervill synthesizer
    val info = MidiSystem.getMidiDeviceInfo.filter(_.getName == "Gervill").headOption
    println(s"info $info")
    val device = info.map(MidiSystem.getMidiDevice).getOrElse {
      println("[ERROR] Could not find Gervill synthesizer.")
      sys.exit(1)
    }
    // Setup output device
    val rcvr = synth.getReceiver
    //    device.open()
    //    val rcvr = device.getReceiver()

    def noteOn(key: Int, gateTime: Long) = {
      val msg = new ShortMessage

      msg.setMessage(NOTE_ON, 0, key, 93)
      rcvr.send(msg, -1)

      Thread.sleep(gateTime)

      msg.setMessage(NOTE_OFF, 0, key, 0)
      rcvr.send(msg, -1)
    }

    val bpm = 66
    // 16th note (semiquaver) duration in milli second
    val sq = 60L * 1000 / bpm / 4

    val notes = Seq((67, sq), (67, sq), (67, sq), (67, sq), (66, sq), (64, 4 * sq), (61, 4 * sq / 3), (62, 4 * sq / 3), (64, 4 * sq / 3), (64, sq), (62, sq), (62, 14 * sq))
    println("start playing")
    notes foreach { case (key, gateTime) => noteOn(key, gateTime) }
    println("finished")
    device.close()
  }
}

object PlayMidi extends App {

  val receivingDevice = getReceivingDevice
  receivingDevice.open()
  val url1 = new URL("http://pscode.org/media/EverLove.mid")
  val sequence1 = MidiSystem.getSequence(url1)
  val sequencer1 = MidiSystem.getSequencer(false)
  val tx1 = sequencer1.getTransmitter
  val rx1 = receivingDevice.getReceiver
  tx1.setReceiver(rx1)
  sequencer1.open()
  sequencer1.setSequence(sequence1)
  val url2 = new URL("http://pscode.org/media/AftrMdnt.mid")
  val sequence2 = MidiSystem.getSequence(url2)
  val sequencer2 = MidiSystem.getSequencer(false)
  val tx2 = sequencer2.getTransmitter
  val rx2 = receivingDevice.getReceiver
  tx2.setReceiver(rx2)
  sequencer2.open()
  sequencer2.setSequence(sequence2)
  sequencer1.start()
  JOptionPane.showMessageDialog(null, "Everlasting Love")
  sequencer2.start()
  JOptionPane.showMessageDialog(null, "After Midnight")

  private def getReceivingDevice: MidiDevice = {
    for (mdi <- MidiSystem.getMidiDeviceInfo) {
      val dev = MidiSystem.getMidiDevice(mdi)
      if (dev.getMaxReceivers != 0) {
        val lcName = mdi.getName.toLowerCase(Locale.ENGLISH)
        if (lcName.contains("java")) {
          return dev
        }
      }
    }
    null
  }
}