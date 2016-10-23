package sonification.ui.right

import java.awt.event.{ ActionEvent, ActionListener }
import java.text.SimpleDateFormat

import scala.concurrent.ExecutionContext.Implicits
import scala.concurrent.Future

import org.deckfour.xes.model.XLog

import javax.swing.{ JTextArea, RootPaneContainer }
import sonification.controller.Controller
import sonification.openxes.OpenXESHelper.RichLog
import sonification.openxes.ParallelismCounter
import sonification.openxes.ParallelismCounter.filterDupes
import sonification.ui.CursorDeluxe
import sonification.ui.SwingHelper.edt

object LogInfoTab {

  private val sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss.SSS")

  def calc(log: XLog): String = {

    def frequency[A](seq: Seq[A], max: Int = Int.MaxValue): String =
      seq.distinct
        .map { e => (e, seq.count(_ == e)) }
        .sortBy(_._2)
        .reverse
        .take(max)
        .map { case (value, frequency) => s"$value ($frequency)" }
        .mkString(", ")

    def values[A](seq: Seq[A], max: Int = Int.MaxValue)(implicit ord: Ordering[A]): String =
      seq.distinct
        .sorted
        .take(max)
        .mkString(", ")

    val (start, end, duration) = log.firstStartLastEndTotalDuration
    val times = log.timeTransitionAttributeValues map sdf.format
    val conceptNames = log.conceptNameAttributeValues
    val transitions = log.lifecycleTransitionAttributeValues
    val resources = log.organizationResourceAttributeValues
    val roles = log.organizationRoleAttributeValues
    val groups = log.organizationGroupAttributeValues
    val costs = log.costTotalAttributeValues

    val parallelism = {
      filterDupes(ParallelismCounter.parallelism(log))  map { e =>
        val time = sdf.format(e.time)
        s"$time -> ${e.parallelism}"
      } mkString (", ")
    }

    val sep = "-" * 30
    val buffer = new StringBuilder(
      s"""Traces: ${log.size}
       |Events: ${log.eventCount}
       |$sep
       |time:timestamp
       |Usage ${times.size}/${log.eventCount}
       |Values: ${values(times)}
       |Frequency: ${frequency(times)}
       |""".stripMargin)
    if (!conceptNames.isEmpty) buffer ++=
      s"""$sep
       |concept:name
       |Usage ${conceptNames.size}/${log.eventCount}
       |Values: ${values(conceptNames)}
       |Frequency: ${frequency(conceptNames)}
       |""".stripMargin
    if (!transitions.isEmpty) buffer ++=
      s"""$sep
       |lifecycle:transition
       |Usage ${transitions.size}/${log.eventCount}
       |Values: ${values(transitions)}
       |Frequency: ${frequency(transitions)}
       |""".stripMargin
    if (!resources.isEmpty) buffer ++=
      s"""$sep
       |org:resource
       |Usage ${resources.size}/${log.eventCount}
       |Values: ${values(resources)}
       |Frequency: ${frequency(resources)}
       |""".stripMargin
    if (!roles.isEmpty) buffer ++=
      s"""$sep
       |org:role
       |Usage ${roles.size}/${log.eventCount}
       |Values: ${values(roles)}
       |Frequency: ${frequency(roles)}
       |""".stripMargin
    if (!groups.isEmpty) buffer ++=
      s"""$sep
       |org:group
       |Usage ${groups.size}/${log.eventCount}
       |Values: ${values(groups)}
       |Frequency: ${frequency(groups)}
       |""".stripMargin
    if (!costs.isEmpty) buffer ++=
      s"""$sep
       |cost:total
       |Usage ${costs.size}/${log.eventCount}
       |Values: ${values(costs)}
       |Frequency: ${frequency(costs)}
       |""".stripMargin
    buffer ++= s"$sep\nParallelism\n"
    buffer ++= parallelism
    buffer.toString
  }
}

class LogInfoTab(implicit ctrl: Controller) extends TabPanel with CursorDeluxe with ActionListener {
  // TODO use swingworker
  import scala.concurrent.ExecutionContext.Implicits.global

  private var tla: Option[RootPaneContainer] = None

  private val textArea = new JTextArea
  //  private val scrollPane = new JScrollPane(textArea)

  private var info: Option[String] = None

  private def recalculate() {
    info = Some(LogInfoTab.calc(ctrl.traces.log))
  }

  def update() {
    info = None
  }

  // initial: set to dirty and
  //  scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS)
  textArea.setLineWrap(true)
  textArea.setWrapStyleWord(true)
  add(textArea)

  private def fillContent() {
    waitCursor(tla.get)
    Future(recalculate()).onComplete { _ =>
      edt {
        info foreach textArea.setText
        normalCursor(tla.get)
      }
    }
  }

  def actionPerformed(e: ActionEvent) {
    if (info != null && info.isEmpty && tla != null && tla.isDefined) {
      fillContent()
    }
  }

  override def repaint() {
    if (tla != null && tla.isEmpty) {
      getTopLevelAncestor match {
        case rpc: RootPaneContainer =>
          tla = Some(rpc)
          fillContent()
        case _ =>
      }
    }
    super.repaint()
  }
}