package sonification.ui.right

import java.awt.{ Color, Dimension }

import scala.collection.mutable.Buffer

import org.processmining.framework.util.ui.widgets.{ ProMHeaderPanel, ProMScrollPane, WidgetColors }

import com.typesafe.scalalogging.StrictLogging

import javax.swing.{ JComponent, JPanel, ScrollPaneConstants }
import sonification.controller.{ Controller, Modifiable }
import sonification.ui.sf

// GUI - Mapping and Player
class Right(implicit ctrl: Controller) extends ProMHeaderPanel(null) with Modifiable with StrictLogging {

  // each mapping has its own tab
  val tabs = sf.createTabbedPane("", WidgetColors.COLOR_LIST_BG, WidgetColors.COLOR_LIST_FG, Color.GREEN)
  tabs.setPreferredSize(new Dimension(1000, 1000))

  // mapping for concept
  val globalMapping = new GlobalMappingPanel(this)
  val tracesSelection = new TracesSelection(this)
  private def scrollable(panel: JPanel) = new ProMScrollPane(panel) {
    // http://stackoverflow.com/questions/12911506/why-jscrollpane-does-not-react-to-mouse-wheel-events
    // TODO doesn't work yet
    // removeMouseWheelListener(this.getMouseWheelListeners()(0))
    setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
    setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
  }

  def addMappings() {
    for {
      (category, categoryMapping) <- ctrl.settings.mapping if categoryMapping.sonification.isDefined
    } {
      val tab = scrollable(new ConcreteMappingTab(category, categoryMapping, this))
      tabs.addTab(category.toString, addToBuffer(tab))
    }
  }

  private val tabsBuffer = Buffer[JComponent]()
  private def addToBuffer(c: JComponent): JComponent = (tabsBuffer += c).last

  def resetTabs() = {
    logger.debug(s"resetTabs ${tabsBuffer.size}")
    tabsBuffer foreach tabs.removeTab
    tracesSelection.removeSelectionPanels()
  }

  val xmlTab = new XmlTab()
  val staccatoTab = new StaccatoTab()
  val parallelismTab = new ParallelismTab()
  val logInfoTab = new LogInfoTab()

  private val metricsTab = new MetricsTab

  // add defined tabs
  def addBasicTabs() {
    tabs.addTab("Mapping", addToBuffer(scrollable(globalMapping)))
    // TODO what was meant by the time mapping
    // tabs.addTab("Time", addToBuffer(new JPanel()))
    tabs.addTab("Traces", addToBuffer(scrollable(tracesSelection)))
    tabs.addTab("Log", addToBuffer(scrollable(logInfoTab)))
    tabs.addTab("XML", addToBuffer(scrollable(xmlTab)), xmlTab)
    tabs.addTab("Staccato", addToBuffer(scrollable(staccatoTab)), staccatoTab)
    tabs.addTab("P", addToBuffer(parallelismTab), parallelismTab)
    tabs.addTab("Metrics", addToBuffer(scrollable(metricsTab)))
  }
  val player = new Player
  add(player)
  add(tabs)
  val properties = new Properties
  add(properties)
}
