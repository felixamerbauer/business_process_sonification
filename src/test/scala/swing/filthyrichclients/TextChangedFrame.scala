package swing.filthyrichclients

import java.awt.{ BorderLayout, Dimension }
import java.awt.event.{ ActionEvent, ActionListener }

import TextChangedFrame.{ DELETING, EDITING, PREF_H, PREF_W, STOPPED_EDITING, TIMER_DELAY }
import javax.swing.{ JFrame, JLabel, JPanel, JTextField, SwingUtilities, Timer }
import javax.swing.event.{ DocumentEvent, DocumentListener }

object TextChangedFrameRunner extends App {
  SwingUtilities.invokeLater(new Runnable() {

    def run() {
      TextChangedFrame.createAndShowGui()
    }
  })

}

object TextChangedFrame {

  val STOPPED_EDITING = "No Longer Editing or Deleting"

  private val EDITING = "Editing"

  private val DELETING = "Deleting"

  private val TIMER_DELAY = 2000

  private val PREF_W = 400

  private val PREF_H = 100

  def createAndShowGui() {
    val mainPanel = new TextChangedFrame()
    val frame = new JFrame("TextChangedFrame")
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE)
    frame.getContentPane.add(mainPanel)
    frame.pack()
    frame.setLocationByPlatform(true)
    frame.setVisible(true)
  }

}

class TextChangedFrame extends JPanel {

  private val textField = new JTextField("Put your text here")

  private val label = new JLabel("You have written: ")

  private val timerListener = new TimerListener()

  private val writeDeleteTimer = new Timer(TIMER_DELAY, timerListener)

  setLayout(new BorderLayout())

  add(textField, BorderLayout.CENTER)

  add(label, BorderLayout.SOUTH)

  textField.getDocument.addDocumentListener(new DocumentListener() {

    def insertUpdate(e: DocumentEvent) {
      label.setText(EDITING)
      writeDeleteTimer.restart()
    }

    def removeUpdate(e: DocumentEvent) {
      label.setText(DELETING)
      writeDeleteTimer.restart()
    }

    def changedUpdate(e: DocumentEvent) {
    }
  })

  override def getPreferredSize(): Dimension = new Dimension(PREF_W, PREF_H)

  private class TimerListener extends ActionListener {

    override def actionPerformed(evt: ActionEvent) {
      label.setText(STOPPED_EDITING)
      val timer = evt.getSource.asInstanceOf[Timer]
      timer.stop()
    }
  }
}
