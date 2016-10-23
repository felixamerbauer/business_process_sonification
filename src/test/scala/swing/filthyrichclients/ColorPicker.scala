package swing.filthyrichclients

import java.awt.{ BorderLayout, Color, Component }
import java.awt.event.{ ActionEvent, ActionListener }

import javax.swing.{ JButton, JColorChooser, JFrame }

object ColorPicker extends App {
  val cp = new ColorPicker()
  cp.setVisible(true)
}

class ColorPicker extends JFrame("JColorChooser Test Frame") {
  setSize(200, 100)
  val contentPane = getContentPane

  val go = new JButton("Show JColorChooser")
  go.addActionListener(new ActionListener() {
    override def actionPerformed(e: ActionEvent) {
      var c = JColorChooser.showDialog(e.getSource.asInstanceOf[Component].getParent, "Demo", Color.blue)
      contentPane.setBackground(c)
    }
  })

  contentPane.add(go, BorderLayout.SOUTH)
  setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE)
}
