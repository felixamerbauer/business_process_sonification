package sonification.openxes

import java.io.File

object OpenXESContinuous extends App {

  MyXesParser.parse(new File("data/xes/generated.xes")) foreach { log =>
    val logString = print(log)
    val logIt = log.iterator()

    while (logIt.hasNext()) {
      val trace = logIt.next()
      val traceIt = trace.iterator()
      while (traceIt.hasNext()) {
        val event = traceIt.next()
        val costs = OpenXESHelper.costTotalAttributeValue(event)
        println(costs)
      }
    }
  }
}