package sonification.controller

import java.io.StringWriter
import scala.concurrent.duration.FiniteDuration
import sonification.controller.MappingModel.Mapping
import scala.collection.mutable.{ Map => MMap }
import java.io.ByteArrayOutputStream
import java.io.ObjectOutputStream
import java.io.ObjectInputStream
import java.io.ByteArrayInputStream

/** gui settings (player and mapping) */
@SerialVersionUID(100L)
case class Settings(
    // mapping
    mapping: Mapping,
    // selected traces (0 based)
    traces: Set[Int],
    // player
    volume: Int = 127,
    speed: Double = 1,
    eventSpeed: Option[Double] = Some(1),
    duration: Option[FiniteDuration] = None,
    eventDurationSpeed: Option[Double] = None,
    metronome: Boolean = false,
    zoomLevel: ZoomLevel = FitPage,
    // save if the current config allows to generate audio
    var sonificationOk: Boolean = true) extends Serializable {
  import Settings.ColorMapping
  
  override def toString = {
    val mappingToString = mapping.mkString("\n")
    s"""$mappingToString\ntraces=$traces\nvolume=$volume, speed=$speed, eventSpeed=$eventSpeed, duration=$duration, eventDurationSpeed=$eventDurationSpeed, metronom=$metronome, zoomLevel=$zoomLevel"""
  }

  def serialize: Array[Byte] = {
    val baos = new ByteArrayOutputStream()
    val oos = new ObjectOutputStream(baos)
    oos.writeObject(this)
    oos.close
    baos.toByteArray
  }

  def clipboard(tracesCount: Int): String = {
    def clipboardE(m: MMap[String, _ <: Enabler[_]]): String = {
      (m.map {
        case (k, v) => s"$k=${v.value} ${if (v.enabled) "x" else ""}"

      }).toSeq.sorted.mkString("\t", "\n\t", "")
    }
    def clipboardColor(m: MMap[String, Enabler[java.awt.Color]]): String = {
      (m.map {
        case (k, v) => s"$k=${ColorMapping.get(v.value).getOrElse(v.value)} ${if (v.enabled) "x" else ""}"

      }).toSeq.sorted.mkString("\t", "\n\t", "")
    }
    def clipboardV(vm: VisualizationMapping): String = vm match {
      case cm: ColorMapping => s"Color\n${clipboardColor(cm.values)}"
      case sm: ShapeMapping => s"Shape\n${clipboardE(sm.values)}"
    }
    def clipboardS(sm: SonificationMapping): String = sm match {
      case im: InstrumentMapping => s"Instrument\n${clipboardE(im.values)}"
      case mm: MelodyMapping => s"Melody\n${clipboardE(mm.values)}"
      case dm: DrumMapping => s"Drum Instrument\n${clipboardE(dm.values)}"
      case rm: RhythmMapping => s"Rhythm\n${clipboardE(rm.values)}"
      case vm: VolumeMapping => s"Volume"
      case pm: PanningMapping => s"Panning"
    }

    val mappingClipboard = {
      val noneEmptyMappings = mapping.filter { case (_, categoryMapping) => categoryMapping.visualization.isDefined || categoryMapping.sonification.isDefined }
      (noneEmptyMappings map {
        case (category, mappings) =>
          s"$category->\n${mappings.sonification.map(clipboardS).getOrElse("-")}\n${mappings.visualization.map(clipboardV).getOrElse("-")}"
      }).mkString("\n")
    }
    s"""$mappingClipboard
       |traces=${if(tracesCount == traces.size) "all" else traces.toSeq.map(_+1).sorted.mkString(", ")}
       |speed=$speed
       |eventSpeed=${eventSpeed.getOrElse("-")}
       |duration=${duration.getOrElse("-")}
       |eventDurationSpeed=${eventDurationSpeed.getOrElse("-")}
       |metronome=$metronome
       |""".stripMargin
  }
}

object Settings {
  
  import java.awt.Color._
  
  val ColorMapping = Map(
      red -> "red",
      green -> "green", 
      blue -> "blue", 
      pink -> "pink", 
      yellow -> "yellow", 
      gray -> "gray", 
      magenta -> "magenta", 
      orange -> "orange", 
      cyan-> "cyan")
  
  def deserialize(bytes: Array[Byte]): Settings = {
    val ois = new ObjectInputStream(new ByteArrayInputStream(bytes))
    val deserialized = ois.readObject.asInstanceOf[Settings]
    ois.close
    deserialized
  }
}
