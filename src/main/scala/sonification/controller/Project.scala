package sonification.controller

import java.io.{ ByteArrayInputStream, ByteArrayOutputStream }
import java.nio.charset.StandardCharsets.UTF_8
import java.util.zip.{ ZipEntry, ZipInputStream, ZipOutputStream }

import org.deckfour.xes.model.XLog

import com.typesafe.scalalogging.StrictLogging

import sonification.openxes.MyXesParser
import sonification.openxes.OpenXESHelper.{ PrettyPrinter120wide4indent, serialize => serializeLog }

case class Project(log: XLog, settings: Settings) {
  private def zip(content: Iterable[(String, Array[Byte])]): Array[Byte] = {
    val bos = new ByteArrayOutputStream
    val zip = new ZipOutputStream(bos)

    content.foreach {
      case (name, data) =>
        zip.putNextEntry(new ZipEntry(name))
        zip.write(data)
        zip.closeEntry()
    }
    zip.close()
    bos.toByteArray
  }

  def serialize: Array[Byte] = {
    val logString = serializeLog(log, PrettyPrinter120wide4indent)
    val settingsBytes = settings.serialize
    val zipEntryLog = (Project.LogEntryName, logString.getBytes(UTF_8))
    val zipEntrySettings = (Project.SettingsEntryName, settingsBytes)
    zip(Seq(zipEntryLog, zipEntrySettings))
  }

}

object Project extends StrictLogging {
  val LogEntryName = "log.xes"
  val SettingsEntryName = "settings.xml"

  def deserialize(data: Array[Byte]): Option[Project] = {
    val zis = new ZipInputStream(new ByteArrayInputStream(data))

    def readZipEntry(): Array[Byte] = {
      val buffer = new Array[Byte](1024)
      val os = new ByteArrayOutputStream
      var read = zis.read(buffer)
      while (read > 0) {
        os.write(buffer, 0, read)
        read = zis.read(buffer)
      }
      os.toByteArray
    }

    var ze = zis.getNextEntry
    var log: XLog = null
    var settings: Settings = null
    while (ze != null) {
      ze.getName match {
        case LogEntryName if log == null =>
          log = MyXesParser.parse(new ByteArrayInputStream(readZipEntry()))
        case SettingsEntryName if settings == null =>
          val bytes = readZipEntry()
          settings = Settings.deserialize(bytes)
        case e => logger.warn(s"Found unexpected log entry $e")
      }
      ze = zis.getNextEntry
    }
    if (log != null && settings != null) Some(Project(log, settings))
    else {
      logger.warn(s"log ($LogEntryName found ${log != null}) or settings ($SettingsEntryName found ${settings != null}) not found")
      None
    }
  }

}