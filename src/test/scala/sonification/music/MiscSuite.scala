package sonification.music

import scala.concurrent.ExecutionContext.Implicits

import org.jfugue.player.Player
import org.scalatest.{ FunSuite, Matchers }

import sonification.music.MusicCommons.Microsecond

class MiscSuite extends FunSuite with Matchers {
  import scala.concurrent.ExecutionContext.Implicits.global

  def sequence(ms: String) = new Player().getSequence(ms)
  
  test("durations"){
    val ms120 = "T120 X[BALANCE_COARSE]=64 X[VOLUME_COARSE]=95 V0  @0.0 I[Piano] 67s 71s Rs Rs 76q V1  @1.46484375 I[Piano]  76s 72s Rs Rs 67q V2  @2.9296875 I[Celesta] 67s 71s Rs Rs 76q V3  @4.39453125 I[Celesta]  76s 72s Rs Rs 67q V4  @5.859375 I[Drawbar_Organ] 67s 71s Rs Rs 76q V5  @7.32421875 I[Guitar] 67s 71s Rs Rs 76q V6  @8.7890625 I[Drawbar_Organ]  76s 72s Rs Rs 67q V7  @10.25390625 I[Guitar]  76s 72s Rs Rs 67q V8  @12.6953125 I[Acoustic_Bass]  67s 67s 67s Rs 67q V10  @14.6484375 I[Violin] 67s 71s Rs Rs 76q V11  @15.625 I[Violin]  67s Rs 72i Rs 67q V12  @17.08984375 I[Violin]  76s 72s Rs Rs 67q V13  @19.53125 I[String_Ensemble_1] 67s 71s Rs Rs 76q V14  @20.5078125 I[String_Ensemble_1]  40h V15  @21.97265625 I[String_Ensemble_1]  76s 72s Rs Rs 67q"
    val ms240 = "T240 X[BALANCE_COARSE]=64 X[VOLUME_COARSE]=95 V0  @0.0 I[Piano] 67s 71s Rs Rs 76q V1  @0.732421875 I[Piano]  76s 72s Rs Rs 67q V2  @1.46484375 I[Celesta] 67s 71s Rs Rs 76q V3  @2.197265625 I[Celesta]  76s 72s Rs Rs 67q V4  @2.9296875 I[Drawbar_Organ] 67s 71s Rs Rs 76q V5  @3.662109375 I[Guitar] 67s 71s Rs Rs 76q V6  @4.39453125 I[Drawbar_Organ]  76s 72s Rs Rs 67q V7  @5.126953125 I[Guitar]  76s 72s Rs Rs 67q V8  @6.34765625 I[Acoustic_Bass]  67s 67s 67s Rs 67q V10  @7.32421875 I[Violin] 67s 71s Rs Rs 76q V11  @7.8125 I[Violin]  67s Rs 72i Rs 67q V12  @8.544921875 I[Violin]  76s 72s Rs Rs 67q V13  @9.765625 I[String_Ensemble_1] 67s 71s Rs Rs 76q V14  @10.25390625 I[String_Ensemble_1]  40h V15  @10.986328125 I[String_Ensemble_1]  76s 72s Rs Rs 67q"
    val ms480 = ms120
    
    MusicCommons.duration(ms120).toMs shouldEqual 44945
    MusicCommons.duration(ms240).toMs shouldEqual 22500
    MusicCommons.duration(ms480).toMs shouldEqual 11125
    
  }

  test("ticks vs duration ms") {
    def calc(ms: String): (Long, Microsecond) = {
      val mp = new Player().getManagedPlayer
      val seq = sequence(ms)
      mp.start(seq)
      mp.pause()
      println(s"DivisionType ${seq.getDivisionType}")
      println(s"MicrosecondLength ${seq.getMicrosecondLength}")
      println(s"Resolution ${seq.getResolution}")
      val result = (mp.getTickLength, MusicCommons.duration(ms))
      mp.finish()
      result
    }
    {
      val (tickLength, durationMs) = calc("T120 C D E F G A B C")
      tickLength shouldEqual 1024
      durationMs.toMs shouldEqual 4000
    }
    {
      val (tickLength, durationMs) = calc("T240 C D E F G A B C")
      tickLength shouldEqual 1024
      durationMs.toMs shouldEqual 2000
    }
  }

}
