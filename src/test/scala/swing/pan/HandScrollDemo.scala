package swing.pan

import java.awt.{ Color, Component, EventQueue, Graphics, Graphics2D, Point, Rectangle, TexturePaint }
import java.awt.event.{ MouseAdapter, MouseEvent }
import java.awt.image.BufferedImage

import HandScrollDemo.{ HandScrollListener, makeCheckerTexture }
import javax.swing.{ BorderFactory, Icon, JComponent, JFrame, JLabel, JScrollPane, JViewport, WindowConstants }

// http://stackoverflow.com/questions/13341857/how-to-pan-an-image-using-your-mouse-in-java-swing
object HandScrollDemo {

  class HandScrollListener extends MouseAdapter {

    private val pp = new Point()

    override def mouseDragged(e: MouseEvent) {
      val vport = e.getSource.asInstanceOf[JViewport]
      val label = vport.getView.asInstanceOf[JComponent]
      val cp = e.getPoint
      val vp = vport.getViewPosition
      vp.translate(pp.x - cp.x, pp.y - cp.y)
      label.scrollRectToVisible(new Rectangle(vp, vport.getSize()))
      pp.setLocation(cp)
    }

    override def mousePressed(e: MouseEvent) {
      pp.setLocation(e.getPoint)
    }
  }

  private def makeCheckerTexture(): TexturePaint = {
    val cs = 20
    val sz = cs * cs
    val img = new BufferedImage(sz, sz, BufferedImage.TYPE_INT_ARGB)
    val g2 = img.createGraphics()
    g2.setPaint(Color.GRAY)
    for (i <- 0 until sz; j <- 0 until sz if (i + j) % 2 == 0) {
      g2.fillRect(i * cs, j * cs, cs, cs)
    }
    g2.dispose()
    new TexturePaint(img, new Rectangle(0, 0, sz, sz))
  }

  def main(args: Array[String]) {
    EventQueue.invokeLater(new Runnable() {

      override def run() {
        createAndShowGUI()
      }
    })
  }

  def createAndShowGUI() {
    val f = new JFrame()
    f.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE)
    f.getContentPane.add(new HandScrollDemo().makeUI())
    f.setSize(320, 320)
    f.setLocationRelativeTo(null)
    f.setVisible(true)
  }
}

class HandScrollDemo {

  def makeUI(): JComponent = {
    val label = new JLabel(new Icon() {

      var TEXTURE: TexturePaint = makeCheckerTexture()

      override def paintIcon(c: Component, g: Graphics, x: Int, y: Int) {
        var g2 = g.create().asInstanceOf[Graphics2D]
        g2.setPaint(TEXTURE)
        g2.fillRect(x, y, c.getWidth, c.getHeight)
        g2.dispose()
      }

      override def getIconWidth(): Int = return 2000

      override def getIconHeight(): Int = return 2000
    })
    label.setBorder(BorderFactory.createLineBorder(Color.RED, 20))
    val scroll = new JScrollPane(label)
    val vport = scroll.getViewport
    val ma = new HandScrollListener()
    vport.addMouseMotionListener(ma)
    vport.addMouseListener(ma)
    scroll
  }
}
