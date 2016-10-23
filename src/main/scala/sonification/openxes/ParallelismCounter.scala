package sonification.openxes

import java.util.Date

import scala.collection.JavaConversions.asScalaBuffer

import org.deckfour.xes.extension.std.XTimeExtension
import org.deckfour.xes.model.{ XEvent, XLog }

object ParallelismCounter extends App {

  case class StartEnd(start: Date, end: Date)

  case class Measurement(time: Date, parallelism: Int)

  private def eventTime(event: XEvent): Date = XTimeExtension.instance().extractTimestamp(event)

  private def allEventsSorted(log: XLog): Seq[XEvent] = {
    (0 until log.size flatMap (idx => log.get(idx))).sortBy(eventTime)
  }

  private def calcStartEnds(log: XLog): Seq[StartEnd] = {
    // get start end of all traces
    0 until log.size map { idx =>
      val trace = log.get(idx)
      StartEnd(eventTime(trace.head), eventTime(trace.last))
    }
  }

  def parallelism(log: XLog): Seq[Measurement] = {
    val size = log.size
    val startEnds = calcStartEnds(log)
    // calculate the parallelism when each event occurs
    allEventsSorted(log) map { e =>
      val time = eventTime(e)
      val parallel = size - startEnds.count(e => time.before(e.start) || time.after(e.end))
      Measurement(time, parallel)
    }
  }

  def filterDupes(s: Seq[Measurement]): Seq[Measurement] = {
    def doit(open: Seq[Measurement], done: Seq[Measurement]): Seq[Measurement] = {
      if (open.isEmpty) done
      else {
        val toAdd = open.takeWhile(_.parallelism == open.head.parallelism)
        doit(open.drop(toAdd.size), done :+ toAdd.head)
      }
    }
    doit(s, Seq())
  }

}