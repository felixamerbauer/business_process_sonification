package sonification.openxes

import java.io.ByteArrayOutputStream
import java.util.Date

import scala.collection.JavaConversions.asScalaBuffer
import scala.xml.{ PrettyPrinter, XML }

import org.deckfour.xes.extension.std.XConceptExtension.{ instance => XConceptExtension }
import org.deckfour.xes.extension.std.XCostExtension.{ instance => XCostExtension }
import org.deckfour.xes.extension.std.XLifecycleExtension.{ instance => XLifecycleExtension }
import org.deckfour.xes.extension.std.XOrganizationalExtension.{ instance => XOrganizationalExtension }
import org.deckfour.xes.extension.std.XSemanticExtension.{ instance => XSemanticExtension }
import org.deckfour.xes.extension.std.XTimeExtension.{ instance => XTimeExtension }
import org.deckfour.xes.factory.XFactoryRegistry
import org.deckfour.xes.model.{ XEvent, XLog, XTrace }
import org.deckfour.xes.model.impl.XLogImpl
import org.deckfour.xes.out.XesXmlSerializer

import com.typesafe.scalalogging.StrictLogging

import sonification.Metrics.{ OX_serialize, t }
import sonification.controller.MappingCategory

object OpenXESHelper extends StrictLogging {
  // used for serialization
  private val xFactory = XFactoryRegistry.instance().currentDefault()
  val PrettyPrinter60wide2indent = new PrettyPrinter(60, 2)
  val PrettyPrinter120wide4indent = new PrettyPrinter(120, 4)

  implicit class RichTrace(trace: XTrace) {
    // TODO check at the beginning that time is available for all trace events and each trace has at least two events
    val startEndDuration: (Date, Date, Long) = {
      val head = trace.get(0)
      val last = trace.get(trace.size - 1)
      val start = timeTransitionAttributeValue(head).get
      val end = timeTransitionAttributeValue(last).get
      val duration = end.getTime - start.getTime
      (start, end, duration)
    }
    def eventValues(category: MappingCategory[_, XEvent]): Seq[String] = (for {
      event <- trace
    } yield { category.extractor(event) }).flatten.map(_.toString)

    val name = Option(XConceptExtension.extractName(trace))
  }

  implicit class RichLog(val log: XLog) {
    val firstStartLastEndTotalDuration: (Date, Date, Long) = {
      val startEndDurations = log.map(_.startEndDuration)
      val result = for {
        firstStart <- startEndDurations.map(_._1).sortWith(_ before _).headOption
        lastEnd <- startEndDurations.map(_._2).sortWith(_ after _).headOption
        totalDuration = lastEnd.getTime() - firstStart.getTime()
      } yield {
        (firstStart, lastEnd, totalDuration)
      }
      // fallback values if log is empty
      result.getOrElse {
        val now = new Date()
        (now, now, 0)
      }
    }

    def deepClone: XLog = log.asInstanceOf[XLogImpl].clone().asInstanceOf[XLogImpl]

    def selectTraces(idxs: Set[Int]): XLog = {
      val clone = log.deepClone
      // TODO explain
      ((clone.size - 1) to 0 by -1).filter(!idxs.contains(_)) foreach { idxToRemove =>
        clone.remove(idxToRemove)
      }
      clone
    }

    // place each trace after the end of the preceding trace
    def cloneSequential: XLog = if (log.size < 2) log else {
      val cloned = deepClone
      val starts = cloned.map(_.startEndDuration._1)
      for {
        traceIdx <- 1 until cloned.size
        trace = cloned.get(traceIdx)
        event <- trace
        time <- timeTransitionAttributeValue(event)
      } {
        // take the end of the last trace
        val endLastTrace = cloned(traceIdx - 1).startEndDuration._2
        // the milliseconds passed of this event relative to the first event of the trace
        val passed = timeTransitionAttributeValue(event).get.getTime - starts(traceIdx).getTime
        val adjustedTime = new Date(endLastTrace.getTime + passed + 5000)
        val timeAttribute = xFactory.createAttributeTimestamp(org.deckfour.xes.extension.std.XTimeExtension.KEY_TIMESTAMP, adjustedTime, XTimeExtension)
        event.getAttributes.put("time:timestamp", timeAttribute)
      }
      cloned
    }

    val maxTraceDuration = firstStartLastEndTotalDuration._3

    def eventValues[T](category: MappingCategory[T, XEvent]): Seq[T] = (for {
      trace <- log
      event <- trace
    } yield { category.extractor(event) }).flatten

    def traceValues[T](category: MappingCategory[T, XTrace]): Seq[T] = (for {
      trace <- log
    } yield { category.extractor(trace) }).flatten

    val conceptNameAttributeValues = OpenXESHelper.conceptNameAttributeValues(log)

    val lifecycleTransitionAttributeValues = OpenXESHelper.lifecycleTransitionAttributeValues(log)

    val timeTransitionAttributeValues = OpenXESHelper.timeTransitionAttributeValues(log)

    val semanticModelReferencess = OpenXESHelper.semanticModelReferencess(log)

    val organizationResourceAttributeValues = OpenXESHelper.organizationResourceAttributeValues(log)

    val organizationRoleAttributeValues = OpenXESHelper.organizationRoleAttributeValues(log)

    val organizationGroupAttributeValues = OpenXESHelper.organizationGroupAttributeValues(log)

    val costTotalAttributeValues = OpenXESHelper.costTotalAttributeValues(log)

    val eventCount = log.map(_.size).sum

  }

  // the following comments are all taken directly from the OpenXES specification

  // name attribute represents the name of the event, e.g. the name of the executed activity represented by the event
  def conceptNameAttributeValue(event: XEvent): Option[String] = Option(XConceptExtension.extractName(event))

  // identifier of the activity instance whose execution has generated the event
  def conceptInstanceAttributeValue(event: XEvent): Option[String] = Option(XConceptExtension.extractInstance(event))

  def lifecycleTransitionAttributeValue(event: XEvent): Option[String] = Option(XLifecycleExtension.extractTransition(event))

  def timeTransitionAttributeValue(event: XEvent): Option[Date] = Option(XTimeExtension.extractTimestamp(event))

  def semanticModelReferences(event: XEvent): Seq[String] = XSemanticExtension.extractModelReferences(event)

  def organizationResourceAttributeValue(event: XEvent): Option[String] = Option(XOrganizationalExtension.extractResource(event))

  def organizationRoleAttributeValue(event: XEvent): Option[String] = Option(XOrganizationalExtension.extractRole(event))

  def organizationGroupAttributeValue(event: XEvent): Option[String] = Option(XOrganizationalExtension.extractGroup(event))

  def costTotalAttributeValue(event: XEvent): Option[Double] = {
    // TODO simplify
    val v = XCostExtension.extractTotal(event)
    if (v != null) Some(v) else None
  }

  def costCurrencyAttributeValue(event: XEvent): Option[String] = Option(XCostExtension.extractCurrency(event))

  // Trace
  def conceptNameAttributeValues(trace: XTrace): Seq[String] = trace flatMap conceptNameAttributeValue

  def traceConceptNameAttributeValue(trace: XTrace): Option[String] = trace.name

  def lifecycleTransitionAttributeValues(trace: XTrace): Seq[String] = trace flatMap lifecycleTransitionAttributeValue

  def timeTransitionAttributeValues(trace: XTrace): Seq[Date] = trace flatMap timeTransitionAttributeValue

  def semanticModelReferencess(trace: XTrace): Seq[String] = trace flatMap semanticModelReferences

  def organizationResourceAttributeValues(trace: XTrace): Seq[String] = trace flatMap organizationResourceAttributeValue

  def organizationRoleAttributeValues(trace: XTrace): Seq[String] = trace flatMap organizationRoleAttributeValue

  def organizationGroupAttributeValues(trace: XTrace): Seq[String] = trace flatMap organizationGroupAttributeValue

  def costTotalAttributeValues(trace: XTrace): Seq[Double] = trace flatMap costTotalAttributeValue

  // Log
  def conceptNameAttributeValues(traces: XLog): Seq[String] = traces flatMap conceptNameAttributeValues

  def lifecycleTransitionAttributeValues(traces: XLog): Seq[String] = traces flatMap lifecycleTransitionAttributeValues

  def timeTransitionAttributeValues(traces: XLog): Seq[Date] = traces flatMap timeTransitionAttributeValues

  def semanticModelReferencess(traces: XLog): Seq[String] = traces flatMap semanticModelReferencess

  def organizationResourceAttributeValues(traces: XLog): Seq[String] = traces flatMap organizationResourceAttributeValues

  def organizationRoleAttributeValues(traces: XLog): Seq[String] = traces flatMap organizationRoleAttributeValues

  def organizationGroupAttributeValues(traces: XLog): Seq[String] = traces flatMap organizationGroupAttributeValues

  def costTotalAttributeValues(traces: XLog): Seq[Double] = traces flatMap costTotalAttributeValues

  def serialize(event: XEvent, pp: PrettyPrinter): String = t(OX_serialize) {
    // construct xml event serialization
    val baos = new ByteArrayOutputStream
    val log = xFactory.createLog()
    val trace = xFactory.createTrace()
    log.add(trace)
    trace.add(event)
    new XesXmlSerializer().serialize(log, baos)
    val logString = baos.toString
    // simplify (drop trace and log shell)
    val logXml = XML.loadString(logString)
    val eventXml = logXml \ "trace" \ "event"
    val pretty = new StringBuilder
    pp.format(eventXml.head, pretty)
    pretty.toString
  }

  def serialize(log: XLog, pp: PrettyPrinter): String = t(OX_serialize) {
    // construct xml event serialization
    val baos = new ByteArrayOutputStream
    new XesXmlSerializer().serialize(log, baos)
    val logString = baos.toString
    // simplify (drop trace and log shell)
    val logXml = XML.loadString(logString)
    val pretty = new StringBuilder
    pp.format(logXml, pretty)
    pretty.toString
  }
}

