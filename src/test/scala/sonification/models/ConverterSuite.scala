//package sonification.models
//
//import java.io.File
//import java.util.concurrent.TimeUnit
//import org.scalatest.FunSuite
//import xes.MyXesParser
//import java.util.Calendar
//import java.util.GregorianCalendar
//import com.typesafe.scalalogging.StrictLogging
//
//class ConverterSuite extends FunSuite with StrictLogging{
//  def time[R](desc: String, block: => R): R = {
//    val start = System.nanoTime()
//    val result = block
//    val elapsed = System.nanoTime() - start
//    logger.info(s"Elapsed time for $desc: ${TimeUnit.NANOSECONDS.toMillis(elapsed) / 1000.toDouble}s")
//    result
//  }
//
//  test("parse and convert XES files") {
//    val files = Seq(
//      "exercise1.xes",
//      "exercise2.xes",
//      "exercise3.xes",
//      "exercise4.xes",
//      "exercise5.xes",
//      "exercise6.xes",
//      "repairExample.xes",
//      "repairExampleSample2.xes",
//      "testset.xes") map ("data/xes/" + _)
//    files foreach { xesFile =>
//      val log = time(s"parse $xesFile", MyXesParser.parse(new File(xesFile)))
//      val sLog = time(s"convert $xesFile", Converter.convert(log))
//      //      logger.debug(sLog)
//    }
//  }
//
//  test("parse, convert and print XES file") {
//    val log = MyXesParser.parse(new File("data/xes/testset.xes"))
//    val sLog = Converter.convert(log)
//    logger.info(sLog.toString())
//  }
//
//  test("parse, convert and check XES file") {
//    val log = MyXesParser.parse(new File("data/xes/testset.xes"))
//    val sLog = Converter.convert(log)
//    assert(sLog.traces.size === 1)
//    val trace = sLog.traces.head
//    val events = trace.events
//    assert(events.size === 15)
//    assert(events.count(_.attribute(SXLifecycleExtension).isDefined) === 15)
//    assert(events.count(_.attribute(SXTimeExtension).isDefined) === 15)
//    assert(events.count(_.attribute(SXConceptExtension).isDefined) === 15)
//    val lifeCycleCompleteEvents = events.filter(_.hasAttribute(SXLifecycleExtension, "complete"))
//    assert(lifeCycleCompleteEvents.size === 6)
//    assert(trace.startEndDuration === Some((
//      new GregorianCalendar(2013, 0, 1, 0, 0, 0).getTime(),
//      new GregorianCalendar(2013, 0, 1, 0, 0, 45).getTime(),
//      45000)))
//
//  }
//}
