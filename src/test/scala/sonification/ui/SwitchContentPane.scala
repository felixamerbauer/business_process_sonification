package sonification.ui

import java.awt.event.{ ActionEvent, ActionListener }

import org.processmining.framework.util.ui.widgets.ProMHeaderPanel

import javax.swing.{ JButton, JFrame, SwingUtilities }

// Stand alone application entry point
object SwitchContentPane extends App {

  SwingUtilities.invokeLater(new Runnable() {

    override def run() {
      val ex = new DebugFrame
      ex.setVisible(true)
      // TODO improve threading
      Thread.sleep(100)
    }
  })
}

class ViewFirst(parent: DebugFrame) extends ProMHeaderPanel(null) {
  println("ViewFirst")
  val button = new JButton("first")
  button.addActionListener(new ActionListener() {
    override def actionPerformed(e: ActionEvent) {
      println("click")
      parent.init(false)
    }
  })
  add(button)
}

class ViewSecond extends ProMHeaderPanel(null) {
  println("ViewSecond")
  add(new JButton("second"))
}

// main swing frame (instead of ProM embedding)
class DebugFrame extends JFrame {

  def init(first: Boolean) {
    setTitle(s"Run - $first")

    val newView = if (first) new ViewFirst(this) else new ViewSecond

    val contentPane = getContentPane()
    contentPane.removeAll()
    contentPane.add(newView)
    contentPane.revalidate()
    contentPane.repaint()
    SwingUtilities.invokeLater(new Runnable() {
      override def run() {
        println("fake ctrl")
      }
    })
  }

  setSize(1600, 900)
  setLocationRelativeTo(null)
  setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE)
  init(true)
}
