//remove if not needed
package swing

import java.awt.{ BorderLayout, Color, Font, Graphics, Point }
import java.awt.event.{ MouseEvent, MouseListener, MouseMotionListener }

import javax.swing.{ BorderFactory, JFrame, JPanel }

// see http://stackoverflow.com/questions/14961516/mouse-listener-on-graphics-rectangles-java
class SundayPanel extends JPanel with MouseMotionListener with MouseListener {

  setSize(1000, 150)

  setBackground(Color.white)

  setBorder(BorderFactory.createEtchedBorder(Color.orange, Color.red))

  var values: Array[String] = Array("1", "2", "3", "4", "5", "6", "7", "8", "9", "10")

  override def paint(g: Graphics) {
    val font = new Font("Times New Roman", Font.PLAIN, 72)
    g.setFont(font)
    for (i <- 0 until 10) {
      val step = 130 * i
      if ((start != null) && ((end.getX / 130).toInt == i)) {
        g.setColor(Color.orange)
      } else {
        g.setColor(Color.gray)
      }
      g.fill3DRect(step, 0, 130, 100, true)
      g.setColor(Color.black)
      g.drawString(values(i), 50 + step, 65)
    }
    if ((start != null) && (end != null)) {
      g.drawLine(start.getX.toInt, start.getY.toInt, end.getX.toInt, end.getY.toInt)
    }
  }

  var start: Point = _

  var end: Point = _

  override def mouseClicked(e: MouseEvent) {
  }

  override def mousePressed(e: MouseEvent) {
    start = e.getPoint
  }

  override def mouseDragged(e: MouseEvent) {
    end = e.getPoint
    val component = e.getComponent
    println(s"component 1 $component")
    component.repaint()
  }

  override def mouseReleased(e: MouseEvent) {
    val from = (start.getX / 130).toInt
    val to = (end.getX / 130).toInt
    val tmp = values(from)
    values(from) = values(to)
    values(to) = tmp
    start = null
    end = null
    val component = e.getComponent
    println(s"component 2 $component")
    component.repaint()
  }

  override def mouseEntered(e: MouseEvent) {
  }

  override def mouseExited(e: MouseEvent) {
  }

  override def mouseMoved(e: MouseEvent) {
  }
}

object SundayMain {

  def main(args: Array[String]) {
    val sunFrame = new SundayFrame("Sunday Today")
    sunFrame.setVisible(true)
    sunFrame.setSize(1330, 150)
    sunFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE)
  }
}

class SundayFrame(title: String) extends JFrame(title) {

  private var sunPanel: SundayPanel = new SundayPanel()

  sunPanel.addMouseListener(sunPanel)

  sunPanel.addMouseMotionListener(sunPanel)

  add(sunPanel, BorderLayout.CENTER)
}
