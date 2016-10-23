package slickerbox

import java.awt.{ Color, Dimension }

import scala.collection.JavaConversions.asJavaIterable

import org.processmining.framework.util.ui.widgets.{ BorderPanel, ProMComboBox, ProMHeaderPanel, ProMScrollPane, ProMSplitPane, ProMTextField, WidgetColors }

import com.fluxicon.slickerbox.factory.{ SlickerDecorator, SlickerFactory }

import javax.swing.{ BoxLayout, JButton, JFrame }
import javax.swing.{ JPanel, ScrollPaneConstants, SwingUtilities }
import javax.swing.JFrame.EXIT_ON_CLOSE

object SlickerboxTestRunner extends App {

  val doCreateAndShowGUI = new Runnable() {
    def run() {
      val frame = new JFrame()
      frame.setTitle("Simple example")
      frame.setSize(800, 800)
      frame.add(Main)
      frame.setLocationRelativeTo(null)
      frame.setDefaultCloseOperation(EXIT_ON_CLOSE)
      frame.setVisible(true)
      frame.setResizable(true)
    }
  }
  SwingUtilities.invokeLater(doCreateAndShowGUI)
}

object Main extends ProMHeaderPanel(null) {
  val factory = SlickerFactory.instance()
  val decorator = SlickerDecorator.instance()
  object MainSplitPane extends ProMSplitPane() {
    object Visualization extends ProMHeaderPanel("Visualization") {

    }
    object PlayerMapping extends ProMHeaderPanel("Player/Mapping") {
      object Player extends BorderPanel(2, 2) {
        setPreferredSize(new Dimension(1000,300))
        val a = factory.createButton("a")
        val b = factory.createButton("b")
        val c = factory.createButton("c")
        add(a)
        add(b)
        add(c)
      }
      val mappings = factory.createTabbedPane("", WidgetColors.COLOR_LIST_BG, WidgetColors.COLOR_LIST_FG, Color.GREEN)

      class MyMappingsPanel extends JPanel {
        setBackground(WidgetColors.PROPERTIES_BACKGROUND)
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS))
      }

      object Events extends MyMappingsPanel {

        for (i <- 1 to 3) {
          val panel = new BorderPanel(2, 2)

          val cc = new JButton(" ")
          cc.setBackground(Color.blue)
          panel.add(cc)

          val label = factory.createLabel("some label text " + i)
          panel.add(label)

          val a = asJavaIterable(Seq("A", "B", "C").map("Option " + _))
          val combo = new ProMComboBox[String](a)
          combo.setPreferredSize(combo.getMinimumSize())
          panel.add(combo)

          add(panel)
        }

      }
      object Activities extends MyMappingsPanel {
        for (i <- 1 to 40) {
          val panel = new BorderPanel(2, 2)

          val label = factory.createLabel("some label text " + i)
          panel.add(label)

          val tf = new ProMTextField("Some clever text")
          tf.setPreferredSize(tf.getMinimumSize())
          panel add (tf)

          val a = asJavaIterable(Seq("A", "B", "C").map("Option " + _))
          val combo = new ProMComboBox[String](a)
          combo.setPreferredSize(combo.getMinimumSize())
          panel.add(combo)

          add(panel)
        }
      }
      object EventsScrollable extends ProMScrollPane(Events) {
        setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
      }
      object ActivitiesScrollable extends ProMScrollPane(Activities) {
        setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
      }
      mappings.addTab("Events", EventsScrollable)
      mappings.addTab("Activities", ActivitiesScrollable)
      add(Player)
      add(mappings)
    }
    setLeftComponent(Visualization);
    setRightComponent(PlayerMapping)
    setResizeWeight(0.5)
  }
  add(MainSplitPane)
}