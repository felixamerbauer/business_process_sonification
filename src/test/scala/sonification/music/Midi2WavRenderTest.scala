package sonification.music

import java.io.{ File, FileOutputStream }

import javax.sound.midi.MidiSystem

object Midi2WavRenderTest extends App {
  val midiFile = new File("tmp.midi")
  val waveFile = new File("tmp.wav")
  val sequence = MidiSystem.getSequence(midiFile)
  val os = new FileOutputStream(waveFile)
  Midi2WavRender.render(null, sequence, os)
  os.close()
}
