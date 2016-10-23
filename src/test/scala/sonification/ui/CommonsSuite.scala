package sonification.ui

import java.awt.Color.{ gray, lightGray, white }

import org.scalatest.{ FunSuite, Matchers }

import sonification.controller.{ Circle, Colors, ColorsStream, ShapesStream, Square, Star }

class UICommonsSuite extends FunSuite with Matchers {
  test("BasicColorsSteam") {
    ColorsStream.take(3) should equal(Array(white, lightGray, gray))
    ColorsStream.slice(0, Colors.length).force should equal(ColorsStream.slice(Colors.length, Colors.length * 2).force)

  }
  test("ShapesStream") {
    ShapesStream.take(3) should equal(Array(Square, Circle, Star))
  }
}
