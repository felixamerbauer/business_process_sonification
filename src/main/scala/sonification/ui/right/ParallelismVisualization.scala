package sonification.ui.right

import java.awt.{ Color, Dimension, Font, Graphics, Graphics2D }
import java.awt.event.{ ActionEvent, ActionListener }

import sonification.controller.Controller
import sonification.openxes.OpenXESHelper.RichLog
import sonification.openxes.ParallelismCounter
import sonification.openxes.ParallelismCounter.{ Measurement, filterDupes }

class ParallelismTab(implicit ctrl: Controller) extends TabPanel with ActionListener {

  case class Data(m: Seq[Measurement], start: Long, end: Long, duration: Long, max: Int)

  private var data: Option[Data] = None

  def calc: Data = {
    val parNoDupes = {
      val par = ParallelismCounter.parallelism(ctrl.traces.log)
      // filter non changes
      filterDupes(par)
    }
    if (parNoDupes.isEmpty) {
      Data(parNoDupes, -1, -1, -1, -1)
    } else {
      // duration
      val (start, end, duration) = ctrl.traces.firstStartLastEndTotalDuration
      // min, max, diff
      val parValues = parNoDupes.map(_.parallelism)
      val max = parValues.max
//      println(s"start $start, end $end, duration $duration, max $max")
      val calculated = Data(parNoDupes, start.getTime, end.getTime, duration, max)
      data = Some(calculated)
      calculated
    }
  }

  def update() {
    data = None
  }

  // step size on y axis
  // draw horizontal lines one for each 
  def horizontalLines()(implicit data: Data, d: Dimension, g2d: Graphics2D) {
    val yFactor = d.height.toFloat / (data.max + 1)
    1 to data.max foreach { e =>
      val y = Math.round(e * yFactor)
      // line
      g2d.drawLine(0, y, d.width, y)
      // description
      val xDesc = 3
      val yDesc = y - 5
      // draw only if enough space
      if (xDesc + 10 < d.width && yDesc > 0) {
        val font = new Font("Serif", Font.PLAIN, 12);
        val oldColor = g2d.getColor
        g2d.setColor(Color.RED)
        g2d.setFont(font);
        g2d.drawString((data.max - e + 1).toString, xDesc, yDesc);
        g2d.setColor(oldColor)
      }
    }
  }

  def parallelismLines()(implicit data: Data, d: Dimension, g2d: Graphics2D) {
    val yFactor = d.height.toFloat / (data.max + 1)
    val oldColor = g2d.getColor
    g2d.setColor(Color.RED)
    if (data.m.isEmpty) {
      // nothing to draw
    } else if (data.m.size == 1) {
      val y = d.height - Math.round(data.m.head.parallelism * yFactor)
      g2d.drawLine(0, y, d.width, y)
    } else {
      data.m.sliding(2) foreach { e =>
        val Seq(a, b) = e
        // horizontal
        // start point
        val x1 = Math.round((a.time.getTime - data.start) / data.duration.toFloat * d.width)
        val y1 = d.height - Math.round(a.parallelism * yFactor)
        // end point
        val x2 = Math.round((b.time.getTime - data.start) / data.duration.toFloat * d.width)
        g2d.drawLine(x1, y1, x2, y1)
        // vertical
        val y2 = d.height - Math.round(b.parallelism * yFactor)
        g2d.drawLine(x2, y1, x2, y2)
      }
    }
    g2d.setColor(oldColor)
  }

  def actionPerformed(e: ActionEvent) {
    //    println("todo actionPerformed")
  }

  override def paintComponent(g: Graphics) {
    super.paintComponent(g)
    // TODO use non-gui thread
    implicit val da = data getOrElse calc
    implicit val g2d = g.asInstanceOf[Graphics2D]
    implicit val d = getSize()
    horizontalLines()
    parallelismLines()
  }

}
