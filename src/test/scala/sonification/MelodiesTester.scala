package sonification

import java.awt.GridLayout
import java.awt.event.{ ActionEvent, ActionListener }

import scala.concurrent.ExecutionContext.Implicits
import scala.concurrent.Future

import org.jfugue.player.ManagedPlayer

import javax.swing.{ JButton, JFrame, JPanel, SwingUtilities }
import sonification.music.MusicCommons
import sonification.music.MusicCommons.Melodies

object MelodiesTester extends App {

  val doCreateAndShowGUI = new Runnable() {
    def run() {
      val f = new JFrame("MelodiesTester")
      f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE)
      f.setSize(1200, 200)
      f.add(new MelodiesTester)
      f.setVisible(true)
      f.setResizable(false)
    }
  }
  SwingUtilities.invokeLater(doCreateAndShowGUI)
}

class MelodiesTester extends JPanel {
  import scala.concurrent.ExecutionContext.Implicits.global
  setLayout(new GridLayout(4, 5))

  private val player = new ManagedPlayer()

  Melodies foreach { e =>
    val button = new JButton(e)
    button.addActionListener(new ActionListener() {
      override def actionPerformed(ae: ActionEvent) {
        if (player.isPlaying()) {
          player.finish()
        }
        Future {
          player.start(MusicCommons.sequence(e))
        }
      }
    })
    add(button)
  }
}
