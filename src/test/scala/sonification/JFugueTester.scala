package sonification

import java.awt.BorderLayout
import java.awt.event.{ ActionEvent, ActionListener }
import java.io.{ File, FileOutputStream }

import org.jfugue.player.{ ManagedPlayer, Player }

import javax.swing.{ JButton, JFrame, JPanel, JScrollPane, JTextArea, ScrollPaneConstants, SwingUtilities }
import sonification.music.{ Midi2WavRenderS, MusicCommons }

object JFugueTester extends App {

  val doCreateAndShowGUI = new Runnable() {
    def run() {
      val f = new JFrame("JFugue Player")
      f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE)
      f.setSize(500, 500)
      f.add(new JFugueTester)
      f.setVisible(true)
      f.setResizable(true)
    }
  }
  SwingUtilities.invokeLater(doCreateAndShowGUI)
}

class JFugueTester extends JPanel {
  setLayout(new BorderLayout)
  var player: Option[ManagedPlayer] = None

  // Staccato
  val ta = new JTextArea("c e g")
  val scroll = new JScrollPane(ta)
  scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED)
  add(scroll, BorderLayout.CENTER)

  // Controls
  val play = new JButton("Play")
  play.addActionListener(new ActionListener {
    def actionPerformed(ae: ActionEvent) {
      println(s"play ${ta.getText}")
      player = Some {
        val p = new Player
        val mp = p.getManagedPlayer
        mp.start(MusicCommons.sequence(ta.getText))
        mp.pause()
        mp
      }
      player foreach (_.resume)
    }
  })

  val stop = new JButton("Stop")
  stop.addActionListener(new ActionListener {
    def actionPerformed(ae: ActionEvent) {
      println("stop")
      player foreach (_.finish)
    }
  })

  val save = new JButton("Save (to ~/tmp.wav)")
  save.addActionListener(new ActionListener {
    def actionPerformed(ae: ActionEvent) {
      println("save")
      // determine save location
      val home = new File(System.getProperty("user.home"))
      assert(home.isDirectory)
      val file = new File(home, "tmp.wav")
      if (!file.exists) {
        println("Generating sequence")
        val sequence = MusicCommons.sequence(ta.getText)
        println("Saving file")
        val os =new FileOutputStream(file)
        Midi2WavRenderS.render(null, sequence, os)
        os.close()
      }
    }
  })

  val controls = new JPanel
  controls.add(play)
  controls.add(stop)
  controls.add(save)

  add(controls, BorderLayout.PAGE_END)
}
