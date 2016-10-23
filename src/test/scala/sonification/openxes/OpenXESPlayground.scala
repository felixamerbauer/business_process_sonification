package sonification.openxes

import java.io.{ ByteArrayOutputStream, File }
import java.nio.charset.StandardCharsets
import java.nio.file.{ Files, Paths }

import org.deckfour.xes.model.XLog
import org.deckfour.xes.out.XesXmlSerializer

import sonification.openxes.OpenXESHelper.RichLog

object OpenXESPlayground extends App {

  def print(log: XLog): String = {
    val baos = new ByteArrayOutputStream
    new XesXmlSerializer().serialize(log, baos)
    baos.toString()
  }

  def write(data: String, path: String) {
    val logFile = Paths.get(path)
    val writer = Files.newBufferedWriter(logFile, StandardCharsets.UTF_8)
    writer.write(data)
    writer.close()
  }

  MyXesParser.parse(new File("data/xes/generated.xes")) foreach { log =>
    val logString = print(log)
    write(logString, "data/xes/tmp.xes")

    println("*" * 40)
    // clone
    val clonedLog = log.deepClone
    write(print(clonedLog), "data/xes/tmp_clone.xes")

    // trace 0 and 2
    val logTraces0And2 = log.selectTraces(Set(0, 2))
    write(print(logTraces0And2), "data/xes/tmp_traces0and2.xes")
  }

}