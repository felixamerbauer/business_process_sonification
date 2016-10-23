package swing

import java.awt.{ Color, Graphics }

import scala.util.Random

import javax.swing.{ JFrame, JPanel }

package object animation {
  
  def createAndShowGUI(name:String,panel:JPanel) {
    val f = new JFrame(name)
    f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE)
    f.setSize(1000, 1000)
    //    checkerboard.add(new MovingButton("Start Animation"))
    f.add(panel)
    f.setVisible(true)
    f.setResizable(false)
  }

  def background(g: Graphics, width: Int, height: Int) {
    g.fillRect(0, 0, width, height)
    g.setColor(Color.white)
    Random.setSeed(0)
    for (_ <- 1 to 1000 * 1000) {
      g.setColor(new Color(Random.nextInt(255), Random.nextInt(255), Random.nextInt(255)))
      g.fillRect(Random.nextInt(width - 5), Random.nextInt(height - 5), 5, 5)
    }
  }
}