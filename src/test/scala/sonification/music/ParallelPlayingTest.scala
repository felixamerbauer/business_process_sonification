package sonification.music

import org.jfugue.player.Player

object ParallelPlayingTest extends App {
  def midi(staccato: String): Array[Byte] = {
    val sequence = new Player().getSequence(staccato)
    MusicCommons.getMidi(sequence)
  }
  
  def break = println("----")

  val s1 = """
V0 I[Piano] @0 c d
V1 I[Rock_Organ] @0 e f# 
V2 I[Celesta] @0 g a
V0 I[Tuba] @2 c d
V1 I[Tuba] @2 e f# 
V2 I[Tuba] @2 g a"""

  val s2 = """
V0 I[Piano] @0 c d
V1 I[Rock_Organ] @0 e f# 
V2 I[Celesta] @0 g a
V0 I[Piano] @1 c d
V1 I[Rock_Organ] @1 e f# 
V2 I[Celesta] @1 g a"""

  val s3 = """
V0 I[Piano] @0 c d
V1 I[Rock_Organ] @0 e f# 
V2 I[Celesta] @0 g a
V3 I[Piano] @2 c d
V4 I[Piano] @2 e f# 
V5 I[Piano] @2 g a"""

  // important to use a new Playe to generate the sequence
  val midi1 = midi(s1)
  val midi2 = midi(s2)

  new Player().play(s1)
  break  
//  new Player().play(s2)
  break  
//  new Player().play(s3)
  
//  val file = new File("""C:\Users\Felix\Desktop\parallel.midi""")
//  if (file.isFile) {
//    println(s"Deleting file ${file.getAbsolutePath}")
//    assert(file.delete)
//  }
//  val fos = new FileOutputStream(file)
//  fos.write(midi)
//  fos.flush()
//  fos.close()

}