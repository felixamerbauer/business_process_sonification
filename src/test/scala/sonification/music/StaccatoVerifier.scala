package sonification.music

import org.scalatest.{ FunSuite, Matchers }

class StaccatoVerifier extends FunSuite with Matchers with StaccatoHelper {

  test("exercise1.xes - exercise1.staccato") { check("exercise1") }
  test("exercise2.xes - exercise2.staccato") { check("exercise2") }
  test("exercise3.xes - exercise3.staccato") { check("exercise3") }
  test("exercise4.xes - exercise4.staccato") { check("exercise4") }
  test("exercise5.xes - exercise5.staccato") { check("exercise5") }
  test("exercise6.xes - exercise6.staccato") { check("exercise6") }
  test("generated_only_org.xes - generated_only_org.staccato") { check("generated_only_org") }
  test("repairExample.xes - repairExample.staccato") { check("repairExample") }
  test("repairExampleSample2.xes - repairExampleSample2.staccato") { check("repairExampleSample2") }
  test("testset.xes - testset.staccato") { check("testset") }
  test("generated.xes - generated.staccato") { check("generated") }
}