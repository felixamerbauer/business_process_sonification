package sonification.music

import java.io.File
import org.jfugue.player.Player
import org.jfugue.theory.Note

object Channel10DrumsTester extends App {

  val ms = {
    println(s"Percussion instruments size ${Note.PERCUSSION_NAMES.length}")
    val instruments = Note.PERCUSSION_NAMES map (e => s"[$e]q")
    val ms = s"V[Percussion] ${instruments.mkString(" ")}"
    println(ms)
    ms
  }

  def changeSoundbank() {
    val home = System.getProperty("user.home")
    val file = new File(s"$home/FluidR3_GM.sf2")
    MusicCommons.changeSoundbank(file)
  }

  def play() { new Player().play(ms) }

  play()
  changeSoundbank()
  play()
}