package sonification

import java.io.InputStream
import org.deckfour.xes.in.XesXmlParser
import org.deckfour.xes.model.XLog
import javax.swing.JFrame
import javax.swing.SwingUtilities
import sonification.controller.Controller
import sonification.ui.View
import sonification.Metrics._
import sonification.openxes.MyXesParser
import sonification.controller._
import sonification.openxes.OpenXESHelper.RichLog

// Stand alone application entry point
object StandaloneRunner extends App {

  SwingUtilities.invokeLater(new Runnable() {

    override def run() {
      val ex = new StandaloneFrame
      ex.setVisible(true)
      // TODO improve threading
      Thread.sleep(100)
    }
  })
}

// main swing frame (instead of ProM embedding)
class StandaloneFrame extends JFrame {

  def initialize(log: XLog, settings: Settings, xesName: String) {
    setTitle(s"Business Process Sonification - $xesName")
    val controller = new Controller(log, settings, Some(this))
    controller.traces = log.selectTraces(settings.traces)

    SwingUtilities.invokeLater(new Runnable() {
      override def run() {
        val view = new View(controller)
        val contentPane = getContentPane()
        contentPane.removeAll()
        contentPane.add(view)
        contentPane.revalidate()
        contentPane.repaint()
        controller.initView
      }
    })
  }

  setSize(1600, 700)
  setLocationRelativeTo(null)
  setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE)
  // take the XES file from the fat JAR
  val xesStream = getClass().getResourceAsStream("/loadAtStartup.xes")
  val log = MyXesParser.parse(xesStream)
  initialize(log, SettingsUtil.defaultSettings(log), "loadAtStartup.xes")
}

