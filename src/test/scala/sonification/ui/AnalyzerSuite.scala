package sonification.ui

import java.io.File

import org.scalatest.FunSuite

import sonification.openxes.MyXesParser
//
//class AnalyzerSuite extends FunSuite {
//  private def log = Converter.convert(MyXesParser.parse(new File("data/xes/testset.xes")))
//
//  test("parse events") {
//    assert(new Analyzer(log).events === Seq("Supply Mechanical Parts", "Produce Headcap System", "Produce Mechanical Parts", "Process Headcap Quality Information", "variable x", "Perform Surface Treatment", "Delivery"))
//  }
//
//  test("parse lifecycles") {
//    assert(new Analyzer(log).lifecycles === Seq("start", "complete", "variable changed", "warning", "error"))
//  }
//}
