package sonification.ui

import org.scalatest.FunSuite

import sonification.controller.calculatePositionString

class ControllerSuite extends FunSuite {

  test("calculatePositionString") {
//    println(calculatePositionString(0))
//    assert(calculatePositionString(0) === "0:0:0:0:0")
//    assert(calculatePositionString(500) === "0:0:0:0:500")
//    assert(calculatePositionString(1500) === "0:0:0:1:500")
    // pi * 24 hours
    assert(calculatePositionString((Math.PI * 24 * 60 * 60 * 1000).toLong) === "0:0:0:1:500")
  }
}
