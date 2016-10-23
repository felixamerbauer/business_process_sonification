package sonification.openxes

import java.io.File
import sonification.openxes.OpenXESHelper.RichLog

object CloneSequentialTest extends App {
  val log = MyXesParser.parse(new File("data/xes/generated.xes")).get
  val xml = OpenXESHelper.serialize(log, OpenXESHelper.PrettyPrinter120wide4indent)
  println(s"orig\n$xml\n${"-" * 30}")
  val logSerialized = log.cloneSequential
  val xmlSerialized = OpenXESHelper.serialize(logSerialized, OpenXESHelper.PrettyPrinter120wide4indent)
  println(s"serialized\n$xmlSerialized\n${"-" * 30}")
}