package sonification.music

import java.io.OutputStream

import com.sun.media.sound.AudioSynthesizer

import javax.sound.midi.{ Receiver, Sequence, Soundbank }
import sonification.Metrics.{ M2W_findAudioSynthesizer, M2W_render, M2W_send, t }

object Midi2WavRenderS {
  def render(soundbank: Soundbank, sequence: Sequence, os: OutputStream): Unit = t(M2W_render) {
    Midi2WavRender.render(soundbank, sequence, os)
  }
  def findAudioSynthesizer(): AudioSynthesizer = t(M2W_findAudioSynthesizer) {
    Midi2WavRender.findAudioSynthesizer()
  }

  def send(seq: Sequence, recv: Receiver): Double = t(M2W_send) {
    Midi2WavRender.send(seq, recv)
  }
}