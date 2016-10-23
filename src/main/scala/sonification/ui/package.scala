package sonification

import com.fluxicon.slickerbox.factory.SlickerDecorator
import com.fluxicon.slickerbox.factory.SlickerFactory

import javax.swing.JButton

package object ui {
  // for improving the slickr
  val sf = SlickerFactory.instance()
  val sd = SlickerDecorator.instance()

  // convenience methods
  def btn(label: String) = {
    val tmp = new JButton(label)
    tmp.setBounds(100, 60, 100, 30)
    tmp
  }

}