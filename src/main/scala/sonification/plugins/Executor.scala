//package sonification.plugins
//
//import java.io.File
//import java.net.URLClassLoader
//
//import scala.Array.canBuildFrom
//
//import org.deckfour.xes.in.XesXmlParser
//import org.deckfour.xes.model.XLog
//import org.processmining.contexts.uitopia.UIPluginContext
//import org.processmining.framework.plugin.PluginContext
//
//import com.typesafe.scalalogging.StrictLogging
//
//object Executor extends StrictLogging {
//
//  // debugging to print classpath
//  def printClasspath() {
//    val cl = ClassLoader.getSystemClassLoader()
//    val urls = (cl.asInstanceOf[URLClassLoader]).getURLs()
//    val sorted = urls.map(_.toString()).toSeq.sorted
//    logger.debug(sorted.mkString("\n"))
//  }
//
//  def someXesParsing: XLog = {
//    val parser = new XesXmlParser()
//    val file = new File("data/xes/testset.xes")
//    assert(file.exists())
//    val logs = parser.parse(file)
//    logs.get(0)
//  }
//  def run(implicit context: UIPluginContext, log: XLog): XLog = {
//    logger.debug(s"run context=$context / log.size=${log.size}")
//    log
//  }
//  def run(implicit context: PluginContext): XLog = {
//    logger.debug(s"run context=$context")
//    normal("run")
//    val log = someXesParsing
//    log
//  }
//
//}