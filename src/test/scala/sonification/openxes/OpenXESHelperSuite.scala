package sonification.openxes

import java.io.File

import scala.collection.JavaConversions.asScalaBuffer

import org.deckfour.xes.factory.XFactoryRegistry
import org.scalatest.{ FunSuite, Matchers }

import com.typesafe.scalalogging.StrictLogging

class OpenXESHelperSuite extends FunSuite with Matchers with StrictLogging {

  test("parse, convert and print XES file") {
    MyXesParser.parse(new File("data/xes/testset.xes")) foreach { log =>
      for {
        trace <- log
        event <- trace
      } {
        val value = OpenXESHelper.conceptNameAttributeValue(event)
        println(s"$event: $value")
      }
    }
  }

  test("deselect traces") {
    val xFactory = XFactoryRegistry.instance().currentDefault()

  }

}
