package sonification.ui.right

import java.awt.{ Color, Dimension, Graphics, Graphics2D }
import java.io.File

import javax.swing.{ JFrame, JPanel, SwingUtilities }
import sonification.openxes.MyXesParser
import sonification.openxes.OpenXESHelper.RichLog
import sonification.openxes.ParallelismCounter
import sonification.openxes.ParallelismCounter.Measurement

object ParallelismVisualizationTest extends App {

  val doCreateAndShowGUI = new Runnable() {
    def run() {
      val f = new JFrame("ShapesThesis")
      f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE)
      f.setSize(920, 620)
      f.add(new ParallelismVisualizationTest)
      f.setVisible(true)
      f.setResizable(true)
    }
  }
  SwingUtilities.invokeLater(doCreateAndShowGUI)

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

class ParallelismVisualizationTest extends JPanel {
  import ParallelismVisualizationTest._

  val log = MyXesParser.parse(new File("data/xes/repairExampleSample2.xes")).get
  // filter non changes
  val parNoDupes = {
    val par = ParallelismCounter.parallelism(log)
    val tmp = filterDupes(par)
    println(s"before ${par.size}, after ${tmp.size}")
    tmp
  }
  // duration
  val (start, end, duration) = log.firstStartLastEndTotalDuration
  // min, max, diff
  val parValues = parNoDupes.map(_.parallelism)
  val min = parValues.min
  val max = parValues.max
  val diff = max - min
  println(s"start $start, end $end, duration $duration, min $min, max $max, diff $diff")

  // step size on y axis
  // draw horizontal lines one for each 
  def horizontalLines()(implicit d: Dimension, g2d: Graphics2D) {
    val yFactor = d.height.toFloat / (max + 1)
    1 to max foreach { e =>
      val y = Math.round(e * yFactor)
      g2d.drawLine(0, y, d.width, y)
    }
  }

  def parallelismLines()(implicit d: Dimension, g2d: Graphics2D) {
    val yFactor = d.height.toFloat / (max + 1)
    val oldColor = g2d.getColor
    g2d.setColor(Color.RED)
    parNoDupes.sliding(2) foreach { e =>
      val Seq(a, b) = e
      // horizontal
      // start point
      val x1 = Math.round((a.time.getTime - start.getTime) / duration.toFloat * d.width)
      val y1 = d.height - Math.round(a.parallelism * yFactor)
      // end point
      val x2 = Math.round((b.time.getTime - start.getTime) / duration.toFloat * d.width)
      g2d.drawLine(x1, y1, x2, y1)
      // vertical
      val y2 = d.height - Math.round(b.parallelism * yFactor)
      g2d.drawLine(x2, y1, x2, y2)
    }
    g2d.setColor(oldColor)
  }

  override def paintComponent(g: Graphics) {
    implicit val g2d = g.asInstanceOf[Graphics2D]
    implicit val d = getSize()
    horizontalLines()
    parallelismLines()
  }

}
