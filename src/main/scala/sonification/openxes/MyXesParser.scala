package sonification.openxes

import java.io.{ File, InputStream }

import scala.annotation.elidable
import scala.annotation.elidable.ASSERTION
import scala.util.{ Failure, Success, Try }

import org.deckfour.xes.in.XesXmlParser
import org.deckfour.xes.model.XLog

import com.typesafe.scalalogging.StrictLogging

import sonification.Metrics.{ TParseXES, t }

object MyXesParser extends App with StrictLogging {

  def parse(file: File): Option[XLog] = t(TParseXES) {
    Try {
      assert(file.exists())
      val parser = new XesXmlParser()
      val logs = parser.parse(file)
      logs.get(0)
    } match {
      case Success(log) => Some(log)
      case Failure(ex) =>
        logger.error(s"Error while loading xes file ${file.getAbsolutePath}", ex)
        None
    }
  }

  def parse(is: InputStream): XLog = t(TParseXES) {
    val parser = new XesXmlParser()
    val logs = parser.parse(is)
    is.close
    logs.get(0)
  }
}