package sonification.music

import scala.concurrent.ExecutionContext.Implicits

import org.scalatest.{ FunSuite, Matchers }

class PlayerWrapperSuite extends FunSuite with Matchers {
  import scala.concurrent.ExecutionContext.Implicits.global

  // TODO check why it doesn't run from sbt
  def checkMidi() = assume(MusicCommons.MidiInstruments.isSuccess)

  val ms = "T120 X[BALANCE_COARSE]=64 X[VOLUME_COARSE]=95 V0  @0.0 I[Piano] 67s 71s Rs Rs 76q V1  @1.46484375 I[Piano]  76s 72s Rs Rs 67q V2  @2.9296875 I[Celesta] 67s 71s Rs Rs 76q V3  @4.39453125 I[Celesta]  76s 72s Rs Rs 67q V4  @5.859375 I[Drawbar_Organ] 67s 71s Rs Rs 76q V5  @7.32421875 I[Guitar] 67s 71s Rs Rs 76q V6  @8.7890625 I[Drawbar_Organ]  76s 72s Rs Rs 67q V7  @10.25390625 I[Guitar]  76s 72s Rs Rs 67q V8  @12.6953125 I[Acoustic_Bass]  67s 67s 67s Rs 67q V10  @14.6484375 I[Violin] 67s 71s Rs Rs 76q V11  @15.625 I[Violin]  67s Rs 72i Rs 67q V12  @17.08984375 I[Violin]  76s 72s Rs Rs 67q V13  @19.53125 I[String_Ensemble_1] 67s 71s Rs Rs 76q V14  @20.5078125 I[String_Ensemble_1]  40h V15  @21.97265625 I[String_Ensemble_1]  76s 72s Rs Rs 67q"
  test("old school") {
//    Await.ready(PlayerWrapper.play(ms), Duration.Inf)
//    Await.ready(new Player().play(ms), Duration.Inf)
  }
}
