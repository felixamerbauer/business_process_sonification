package swing

import java.awt.{ BorderLayout, Dimension, EventQueue }
import java.awt.event.{ ActionEvent, ActionListener }
import java.awt.image.BufferedImage

import org.jfugue.player.Player

import javax.imageio.ImageIO
import javax.swing.{ ImageIcon, JButton, JEditorPane, JFrame, JLabel, JPanel, JToolTip }

// see http://rcforte.wordpress.com/2010/10/10/custom-jtooltip/
object CustomTooltipSample extends App {
  EventQueue.invokeLater(new Runnable() {
    override def run() {
      val frame = new CustomTooltipSample()
      frame.setVisible(true)
    }
  })
}

class CustomLabel(text: String) extends JLabel(text) {

  lazy val tooltip: CustomTooltip = {
    val tmp = new CustomTooltip()
    tmp.setComponent(this)
    tmp
  }

  override def createToolTip(): JToolTip = tooltip
}

class CustomTooltip extends JToolTip() {

  val info = new JEditorPane()
  info.setContentType("text/html")
  val play = new JButton("Listen to some musics!")
  play.addActionListener(new ActionListener() {
    override def actionPerformed(e: ActionEvent) {
      // TODO play on none UI thread
      new Player().play("c d e f g a b c6")
    }
  })
  val panel = new JPanel(new BorderLayout())
  val image: BufferedImage = {
    val resource = getClass().getResource("/open.png")
    ImageIO.read(resource)
  }
  val picLabel = new JLabel(new ImageIcon(image))

  panel.add(BorderLayout.CENTER, info)
  panel.add(BorderLayout.SOUTH, play)
  panel.add(BorderLayout.NORTH, picLabel)
  setLayout(new BorderLayout())
  add(panel)

  override def getPreferredSize(): Dimension = {
    //    panel.getPreferredSize
    new Dimension(500, 500)
  }

  override def setTipText(tipText: String) {
    if (tipText != null && !tipText.isEmpty) {
      info.setText(tipText)
    } else {
      super.setTipText(tipText)
    }
  }
}

class CustomTooltipSample extends JFrame {
  val label = new CustomLabel("My Label")

  setTitle("Custom tooltip sample")
  setSize(300, 200)
  setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE)
  label.setToolTipText("<html><body>Yo, I am a tooltip with components and <b>HTML</b>!</body></html>")
  add(label)
}
