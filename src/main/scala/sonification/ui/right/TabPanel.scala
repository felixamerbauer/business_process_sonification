package sonification.ui.right

import org.processmining.framework.util.ui.widgets.WidgetColors

import javax.swing.{ BoxLayout, JPanel }

class TabPanel extends JPanel {
  setBackground(WidgetColors.PROPERTIES_BACKGROUND)
  setLayout(new BoxLayout(this, BoxLayout.Y_AXIS))
}
