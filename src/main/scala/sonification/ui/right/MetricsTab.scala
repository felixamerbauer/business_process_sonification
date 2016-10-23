package sonification.ui.right

import java.util.concurrent.TimeUnit.{ MILLISECONDS, NANOSECONDS }

import scala.collection.JavaConversions.mapAsScalaMap
import scala.collection.mutable.{ Map => MMap }

import com.codahale.metrics.{ Snapshot, Timer }

import javax.swing.{ JPanel, JTable }
import sonification.Metrics
import sonification.Metrics.registry
import sonification.controller.code2actionListener
import sonification.ui.sf

class MetricsTab() extends TabPanel {
  private case class Row(name: String, size: Int, min: Long, max: Long, mean: Long, stdDev: Long, median: Long) {
    val data = productIterator.map(_.toString).toArray
    val totalDuration = size * mean
  }
  private val columnNames = Array("Function", "#", "Min", "Max", "Mean", "StdDev", "Median")

  private val updateButton = sf.createButton("Refresh")
  updateButton.addActionListener { update() }
  private val resetButton = sf.createButton("Reset")
  resetButton.addActionListener { reset() }

  val table = new JTable(new MyTableModel(Array[Array[String]](), columnNames))

  private def format(name: String, snapshot: Snapshot): Row = {

    def convert(in: Long): Long = MILLISECONDS.convert(in, NANOSECONDS)

    val size = snapshot.size()
    val min = convert(snapshot.getMin)
    val max = convert(snapshot.getMax)
    val mean = convert(snapshot.getMean.toLong)
    val stdDev = convert(snapshot.getStdDev.toLong)
    val median = convert(snapshot.getMedian.toLong)

    Row(name, snapshot.size(), min, max, mean, stdDev, median)
  }

  def update() {
    val timers: MMap[String, Timer] = registry.getTimers
    val rows = timers map {
      case (name, timer) => format(name, timer.getSnapshot)
    }
    val tableData = rows
      .toSeq
      .sortWith(_.totalDuration > _.totalDuration)
      .map(_.data)
      .toArray
    table.getModel.asInstanceOf[MyTableModel].update(tableData)
  }

  def reset() {
    Metrics.reset()
    update()
  }
  
  add(new JPanel {
    add(updateButton)
    add(resetButton)
  })
  add(table.getTableHeader)
  add(table)
  update()
}
