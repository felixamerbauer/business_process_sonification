package sonification.music

object DefaultInstrumentsTest extends App {
  MusicCommons.JFugueInstrumentsStream.take(256).zipWithIndex foreach {
    case (instrument, idx) =>
      println(s"$idx: $instrument")
  }
}