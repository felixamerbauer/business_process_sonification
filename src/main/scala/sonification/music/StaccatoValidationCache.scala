package sonification.music

import scala.collection.mutable.LinkedHashMap

import com.typesafe.scalalogging.StrictLogging

import sonification.Metrics.{ SVC_validate, t }

object MelodyStaccatoValidationCache extends StrictLogging {
  private val cache = LinkedHashMap[String, Boolean]()

  def valid(staccato: String): Boolean = t(SVC_validate){
    cache.get(staccato).getOrElse {
      val valid = MusicCommons.melodyStaccatoValid(staccato)
      logger.debug(s"adding staccato \n$staccato\nwith state $valid to melody cache")
      cache += staccato -> valid
      if (cache.size > 1000) {
        cache -= cache.keys.head
      }
      valid
    }
  }
}

object RhythmStaccatoValidationCache extends StrictLogging {
  private val cache = LinkedHashMap[String, Boolean]()

  private val validator = """^(q |i |s |t )*(h|q|i|s|t)$""".r 
  
  def valid(staccato: String): Boolean = t(SVC_validate){
    cache.get(staccato).getOrElse {
      val valid = validator.findFirstMatchIn(staccato).isDefined
      logger.debug(s"adding staccato \n$staccato\nwith state $valid to rhythm cache")
      cache += staccato -> valid
      if (cache.size > 1000) {
        cache -= cache.keys.head
      }
      valid
    }
  }
}