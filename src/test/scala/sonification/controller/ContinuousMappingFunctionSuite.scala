package sonification.controller

import org.scalatest.{ FunSuite, Matchers }

import sonification.controller.MappingModel.distribution

class ContinuousMappingFunctionSuite extends FunSuite with Matchers {

  test("Distribution identity") {
    val in = Seq(7, 4, 9, 10, 4, 0, 2, 3).map(_.toDouble)
    distribution(0, 10)(in) shouldEqual in
  }

  test("Distribution identity left stretched") {
    val in = Seq(7, 4, 9, 10, 4, 0, 2, 3).map(_.toDouble)
    distribution(20, 40)(in) shouldEqual Seq(34, 28, 38, 40, 28, 20, 24, 26)
  }

  test("Distribution identity right stretched") {
    val in = Seq(7, 4, 9, 10, 4, 0, 2, 3).map(_.toDouble)
    distribution(-30, -10)(in) shouldEqual Seq(-16, -22, -12, -10, -22, -30, -26, -24)
  }
}
