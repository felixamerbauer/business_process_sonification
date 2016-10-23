package sonification.ui.left

import java.awt.{ BasicStroke, Dimension, Polygon, Rectangle, Shape => AwtShape }
import java.awt.geom.{ Ellipse2D, GeneralPath, Line2D, Path2D }

import scala.Array.canBuildFrom

/**
 * Coordinates
 * @param x x coordinate
 * @param y y coordinate
 */
class XY(val x: Int, val y: Int) {
  override def toString = s"$x/$y"
}

object XY {
  def apply(x: Int, y: Int) = new XY(x, y)
}

/** Draws various simple geometric shapes */
object Shapes {
  val dashedStroke = new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, Array(9.toFloat), 0)

  private def createPolygon(xys: Array[(Int, Int)]): GeneralPath = {
    val path = new GeneralPath(Path2D.WIND_EVEN_ODD, xys.length)
    path.moveTo(xys(0)._1, xys(0)._2)
    for ((x, y) <- xys) {
      path.lineTo(x, y)
    }
    path.closePath()
    path
  }

  def createRectangle(xy: XY, w: Int): AwtShape = {
    new Rectangle(xy.x - w / 2, xy.y - w / 2, w, w)
  }
  def createCircle(xy: XY, r: Int): AwtShape = {
    new Ellipse2D.Double(xy.x - r, xy.y - r, 2 * r, 2 * r)
  }

  private val xysCross = Array((0, 1), (1, 1), (1, 0), (2, 0), (2, 1), (3, 1), (3, 2), (2, 2), (2, 3), (1, 3), (1, 2), (0, 2))

  def createCross(xy: XY, w: Int): GeneralPath = {
    val scale = w / 3
    val xysScaledTranslated = for ((x, y) <- xysCross) yield ((x * scale + xy.x - w / 2, y * scale + xy.y - w / 2))
    createPolygon(xysScaledTranslated)
  }

  def createRhombus(xy: XY, w: Int): GeneralPath = {
    val xys = Array((0, 2), (2, 0), (4, 2), (2, 4))
    val scale = w / 4
    val xysScaledTranslated = for ((x, y) <- xys) yield ((x * scale + xy.x - w / 2, y * scale + xy.y - w / 2))
    createPolygon(xysScaledTranslated)
  }

  def createStar(xy: XY, w: Int): Polygon = new StarPolygon(x = xy.x, y = xy.y, r = w / 2, innerR = w / 4, vertexCount = 8)

  def createGrid(width: Int, height: Int, traces: Int, itemSize: Int): Seq[Line2D.Double] = {
    val effectiveHeight = height - itemSize - VisusalizationCalculator.Padding * 2
    0 until traces map { traceIdx =>
      val y = (traceIdx + 1) * effectiveHeight / (traces + 1) + itemSize / 2 + VisusalizationCalculator.Padding
      new Line2D.Double(0, y, width, y)
    }

  }

  def cell(x: Int, y: Int)(implicit dimension: Dimension): XY = XY(dimension.width / 10 * x, dimension.height / 10 * y)

}