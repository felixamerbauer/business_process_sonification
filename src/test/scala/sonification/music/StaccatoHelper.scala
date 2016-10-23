package sonification.music

import java.io.File
import java.util.LinkedList

import scala.collection.JavaConversions.asScalaBuffer
import scala.io.Codec.UTF8
import scala.io.Source

import org.deckfour.xes.model.XLog
import org.scalatest.Matchers

import com.sksamuel.diffpatch.DiffMatchPatch
import com.sksamuel.diffpatch.DiffMatchPatch.{ Diff, Operation }

import sonification.controller.{ Settings, SettingsUtil }
import sonification.openxes.MyXesParser

trait StaccatoHelper {
  this: Matchers =>

  val dir = "data/xes"

  def generateStaccato(file: String): (String, XLog, Settings) = {
    val xes = MyXesParser.parse(new File(file)).get
    val defaultSettings = SettingsUtil.defaultSettings(xes)
    val staccato = StaccatoGenerator.makeStaccato(xes, defaultSettings).trim
    (staccato, xes, defaultSettings)
  }
  def readStaccato(file: String): String = {
    val source = Source.fromFile(file)(UTF8)
    val text = source.mkString
    source.close
    text.replace("\r", "").trim
  }

  def check(test: String, proof: String) {
    val dmp = new DiffMatchPatch()
    val diffs = dmp.diff_main(test, proof)
    // remove all equal ones
    val nonEqual = new LinkedList[Diff]()
    diffs.filter(_.operation != Operation.EQUAL) foreach (nonEqual.add)

    println("Standard diffs")
    nonEqual foreach println
    dmp.diff_cleanupSemantic(nonEqual)
    println("Semantic diffs")
    nonEqual foreach println
    dmp.diff_prettyHtml(nonEqual)
    println("Pretty HTML diffs")
    nonEqual foreach println
    nonEqual.size shouldEqual 0
  }

  def check(basename: String) {
    val (test,_,_) = generateStaccato(s"$dir/$basename.xes")
    //    write(new File(s"$dir/$basename.test.staccato").toPath, test)
    val proof = readStaccato(s"$dir/$basename.staccato")
    check(test, proof)
  }

}