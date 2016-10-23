package swing.filthyrichclients

import java.awt.{ BorderLayout, Color, Component }
import java.awt.event.{ ActionEvent, ActionListener }

import javax.swing.{ JButton, JColorChooser, JFrame, JPanel, Timer }

object AnimationExample extends App {
  val cp = new AnimationExample()
  cp.setVisible(true)
}

class AnimationPanel extends JPanel {
  val delay = 5000
  val timer = new Timer(delay, new SwingTimerDemo());
  val startTime = System.currentTimeMillis();
  val prevTime = startTime
    timer.start();
}

class AnimationExample extends JFrame("JColorChooser Test Frame") {
  setSize(500, 500)
  val contentPane = getContentPane

  val go = new JButton("Show JColorChooser")
  go.addActionListener(new ActionListener() {
    override def actionPerformed(e: ActionEvent) {
      val c = JColorChooser.showDialog(e.getSource.asInstanceOf[Component].getParent, "Demo", Color.blue)
      contentPane.setBackground(c)
    }
  })

  contentPane.add(go, BorderLayout.SOUTH)
  setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE)
}

