package sonification

import java.awt.GridLayout
import java.awt.event.{ ActionEvent, ActionListener }

import scala.concurrent.ExecutionContext.Implicits
import scala.concurrent.Future

import org.jfugue.player.ManagedPlayer

import javax.swing.{ JButton, JFrame, JPanel, SwingUtilities }
import sonification.music.MusicCommons
import sonification.music.MusicCommons.JFugueInstrumentStrings

object InstrumentsTester extends App {

  val doCreateAndShowGUI = new Runnable() {
    def run() {
      val f = new JFrame("InstrumentsTester")
      f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE)
      f.setSize(1200, 500)
      f.add(new InstrumentsTester)
      f.setVisible(true)
      f.setResizable(false)
    }
  }
  SwingUtilities.invokeLater(doCreateAndShowGUI)
}

class InstrumentsTester extends JPanel {
  import scala.concurrent.ExecutionContext.Implicits.global
  setLayout(new GridLayout(16, 8))

  private val player = new ManagedPlayer()

  for ((name, idx) <- JFugueInstrumentStrings.zipWithIndex) yield {
    val button = new JButton(s"${idx + 1} $name")
    button.addActionListener(new ActionListener() {
      override def actionPerformed(e: ActionEvent) {
        if (player.isPlaying()) {
          player.finish()
        }
        Future {
          player.start(MusicCommons.sequence(s"I[$name] :Controller(7,127) c d e f g a b c"))
        }
      }
    })
    add(button)
  }
}
