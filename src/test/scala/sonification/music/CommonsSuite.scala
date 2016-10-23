package sonification.music

import org.jfugue.pattern.Pattern
import org.scalatest.{ FunSuite, Matchers }

class MusicCommonsSuite extends FunSuite with Matchers {

  // TODO check why it doesn't run from sbt
  def checkMidi() = assume(MusicCommons.MidiInstruments.isSuccess)

  test("fetch list of midi instruments") {
    checkMidi
    val instruments = MusicCommons.MidiInstruments
    instruments.get should have size (235)
  }

  test("check if all JFugue instruments can be used") {
    val ms = MusicCommons.JFugueInstrumentStrings.map(e => s"I[${e}]").mkString(" ")
    println(s"Checking $ms")
    MusicCommons.melodyStaccatoValid(ms) shouldEqual true
  }

  test("duration single note via conversion to midi sequence") {
    checkMidi
    // sequence duration is exact (since jfugue5)
    MusicCommons.duration("T[Adagio] C5w").toMilliseconds should equal(4000)
    MusicCommons.duration("T[Adagio] C5h").toMilliseconds should equal(2000)
    MusicCommons.duration("T[Adagio] C5q").toMilliseconds should equal(1000)
    MusicCommons.duration("T[Adagio] C5i").toMilliseconds should equal(500)
    MusicCommons.duration("T[Adagio] C5s").toMilliseconds should equal(250)
    MusicCommons.duration("T[Adagio] C5t").toMilliseconds should equal(125)
    MusicCommons.duration("T[Adagio] C5x").toMilliseconds should equal(62)
    MusicCommons.duration("T[Adagio] C5o").toMilliseconds should equal(31)
    // default speed is allegro (120 bpm)
    MusicCommons.duration("T[Allegro] C5q").toMilliseconds should equal(500)
  }

  test("duration multiples notes via conversion to midi sequence") {
    checkMidi
    MusicCommons.duration("T[Allegro] Cq Dq Eq Fq").toMilliseconds should equal(2000)
    // default speed is allegro (120 bpm)
    // 8 eighths should have the same duration as 4 quarters
    MusicCommons.duration("Ci Di Ei Fi Gi Ai Bi C6i").toMilliseconds should equal(2000)
    MusicCommons.duration("Cs Ds Es Fs Gs As Bs Cs Ds Es Fs Gs As Bs Cs Ds").toMilliseconds should equal(2000)
  }

  test("duration from pattern object") {
    checkMidi
    MusicCommons.duration(new Pattern("T[Allegro] Cq Dq Eq Fq")).toMilliseconds should equal(2000)
  }

  test("start at (@) in music string") {
    checkMidi
    MusicCommons.duration("Cq").toMilliseconds should equal(500)
    // starting at the first millisecond shouldn't change anything
    MusicCommons.duration("@0 Cq").toMilliseconds should equal(500)
    // starting two notes at the same time
    MusicCommons.duration("@0 Cq @0 Cq").toMilliseconds should equal(500)
    MusicCommons.duration("Cq @0 Cq").toMilliseconds should equal(500)
  }

  test("jFugueInstrumentsStream8") {
    MusicCommons.JFugueInstrumentsStream.take(3).mkString(", ") should equal("Piano, Celesta, Drawbar_Organ")
    val idxToName = Map(MusicCommons.JFugueInstrumentStrings.zipWithIndex: _*)
     MusicCommons.JFugueInstrumentsStream.take(20).map(idxToName) should equal(Seq(0, 8, 16, 24, 32, 40, 48, 56, 64, 72, 80, 88, 96, 104, 112, 120, 1, 9, 17, 25))
  }

  test("staccatoValid") {
    MusicCommons.melodyStaccatoValid("c d e f g") should equal(true)
    MusicCommons.melodyStaccatoValid("@100 c") should equal(true)
    MusicCommons.melodyStaccatoValid("I[100000] c") should equal(false)
    MusicCommons.melodyStaccatoValid("T60 c") should equal(true)
    MusicCommons.melodyStaccatoValid("T-60 c") should equal(false)

  }

//  // TODO check why it doesn't run from sbt
//  test("convert testset.xes to jfugue music string") {
//    checkMidi
//    val log = MyXesParser.parse(new File("data/xes/testset.xes"))
//    val sLog = Converter.convert(log)
//    val controller = new Controller(sLog)
//    val settings = controller.settings
//    val staccato = staccatoGenerator.makestaccato(sLog, settings)
//    println(s"generated music string:\n$staccato")
//    staccato should equal("@0 I[0] [67]s [71]s Rs Rs [76]q @182 I[0]  [76]s [72]s Rs Rs [67]q @363 I[8] [67]s [71]s Rs Rs [76]q @545 I[8]  [76]s [72]s Rs Rs [67]q @726 I[16] [67]s [71]s Rs Rs [76]q @908 I[24] [67]s [71]s Rs Rs [76]q @1089 I[16]  [76]s [72]s Rs Rs [67]q @1271 I[24]  [76]s [72]s Rs Rs [67]q @1573 I[32]  [67]s [67]s [67]s Rs [67]q @1815 I[40] [67]s [71]s Rs Rs [76]q @1936 I[40]  [67]s Rs [72]i Rs [67]q @2118 I[40]  [76]s [72]s Rs Rs [67]q @2421 I[48] [67]s [71]s Rs Rs [76]q @2542 I[48]  [40]h @2723 I[48]  [76]s [72]s Rs Rs [67]q")
//    // PlayerWrapper.player.play(staccato)
//  }

  test("speed") {
    checkMidi
    val base = "c d e f g a b c6"
    val speedsDurations = Seq((0.25, 16000), (0.5, 8000), (1D, 4000), (2D, 2000), (4D, 1000), (8D, 500))
    for ((factor, duration) <- speedsDurations) {
      val speed = (120 * factor).toInt
      val ms = MusicCommons.duration(s"T$speed $base").toMilliseconds
      ms shouldEqual (duration)
    }
  }

}
