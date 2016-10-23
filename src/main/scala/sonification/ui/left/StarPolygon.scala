package sonification.ui.left

import java.awt.Polygon

import StarPolygon.{ getXCoordinates, getYCoordinates }

/**
 * adapted code from http://java-sl.com/shapes.html
 *
 */
object StarPolygon {

   def getXCoordinates(x: Int,
    y: Int,
    r: Int,
    innerR: Int,
    vertexCount: Int,
    startAngle: Double): Array[Int] = {
    val res = Array.ofDim[Int](vertexCount * 2)
    val addAngle = 2 * Math.PI / vertexCount
    var angle = startAngle
    var innerAngle = startAngle + Math.PI / vertexCount
    for (i <- 0 until vertexCount) {
      res(i * 2) = Math.round(r * Math.cos(angle)).toInt + x
      angle += addAngle
      res(i * 2 + 1) = Math.round(innerR * Math.cos(innerAngle)).toInt + x
      innerAngle += addAngle
    }
    res
  }

  def getYCoordinates(x: Int,
    y: Int,
    r: Int,
    innerR: Int,
    vertexCount: Int,
    startAngle: Double): Array[Int] = {
    val res = Array.ofDim[Int](vertexCount * 2)
    val addAngle = 2 * Math.PI / vertexCount
    var angle = startAngle
    var innerAngle = startAngle + Math.PI / vertexCount
    for (i <- 0 until vertexCount) {
      res(i * 2) = Math.round(r * Math.sin(angle)).toInt + y
      angle += addAngle
      res(i * 2 + 1) = Math.round(innerR * Math.sin(innerAngle)).toInt + y
      innerAngle += addAngle
    }
    res
  }
}

class StarPolygon(x: Int, y: Int, r: Int, innerR: Int, vertexCount: Int, startAngle: Double = 0) extends Polygon(
  getXCoordinates(x, y, r, innerR, vertexCount, startAngle),
  getYCoordinates(x, y, r, innerR, vertexCount, startAngle), vertexCount * 2) 

