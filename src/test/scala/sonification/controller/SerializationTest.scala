package sonification.controller

import java.io.File

import sonification.openxes.MyXesParser

object SerializationTest extends App {
  val dir = "data/xes"

  val file = new File(dir, "exercise1.xes")
  val xes = MyXesParser.parse(file).get
  val settings = SettingsUtil.defaultSettings(xes)
  val settingsCopy = settings.copy()
  assert(settings == settingsCopy, "Copies done by copy() on case class not identical")
  val serialized = settings.serialize
  println(serialized)
  val settingsDeserialized = Settings.deserialize(serialized)
  println(s"settings orig\n$settings")
  println(s"settings after serialization and deserialization")
  println(s"settings after\n$settingsDeserialized")
  assert(settings.toString == settingsDeserialized.toString, "Settings after serialization and deserialization not identical to starting point")
}