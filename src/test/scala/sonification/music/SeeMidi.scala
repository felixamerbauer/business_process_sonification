package sonification.music

import java.io.File

import org.jfugue.midi.MidiFileManager
import org.jfugue.player.Player

object SeeMidi extends App {
  val pattern = MidiFileManager.loadPatternFromMidi(new File("""C:\Users\Felix\Desktop\ParallelMuse.mid"""))
  println(pattern)
  new Player().play(pattern)
//
//  val sequence = new Player().getSequence(pattern)
//  val midi = MusicCommons.getMidi(sequence)
//  val file = new File("""C:\Users\Felix\Desktop\ParallelMuse2.mid""")
//  if (file.isFile) {
//    println(s"Deleting file ${file.getAbsolutePath}")
//    assert(file.delete)
//  }
//  val fos = new FileOutputStream(file)
//  fos.write(midi)
//  fos.flush()
//  fos.close()

}