package deployment

import java.io.File
import java.nio.file.{ Files, StandardCopyOption }

object CopyDependencies extends App {
  val files = Seq(
    """C:\Users\Felix\.ivy2\cache\org.swinglabs\swingx\jars\swingx-1.6.1.jar""",
    """C:\Users\Felix\.ivy2\cache\com.jhlabs\filters\jars\filters-2.0.235.jar""",
    """C:\Users\Felix\.ivy2\cache\org.swinglabs\swing-worker\jars\swing-worker-1.1.jar""",
    """C:\Users\Felix\.ivy2\cache\com.typesafe.scala-logging\scala-logging_2.11\jars\scala-logging_2.11-3.1.0.jar""",
    """C:\Users\Felix\.ivy2\cache\org.slf4j\slf4j-log4j12\jars\slf4j-log4j12-1.7.10.jar""",
    """C:\Users\Felix\.ivy2\cache\org.slf4j\slf4j-api\jars\slf4j-api-1.7.10.jar""",
    """C:\Users\Felix\.ivy2\cache\log4j\log4j\bundles\log4j-1.2.17.jar""",
    """C:\Users\Felix\.ivy2\cache\joda-time\joda-time\jars\joda-time-2.7.jar""",
    """C:\Users\Felix\.ivy2\cache\org.joda\joda-convert\jars\joda-convert-1.7.jar""",
    """C:\Users\Felix\.ivy2\cache\org.apache.directory.studio\org.apache.commons.io\jars\org.apache.commons.io-2.4.jar""",
    """C:\Users\Felix\.ivy2\cache\commons-io\commons-io\jars\commons-io-2.4.jar""",
    """C:\Users\Felix\.ivy2\cache\org.scala-lang.modules\scala-xml_2.11\bundles\scala-xml_2.11-1.0.3.jar""")
  files.map(e => new File(e)).foreach { source =>
    println(source)
    val target = new File(new File("""C:\tools\workspace_privat\ProM\scala"""), source.getName)
    println(s"${source.getAbsolutePath} -> ${target.getAbsolutePath}")
    Files.copy(source.toPath(), target.toPath(), StandardCopyOption.REPLACE_EXISTING)
  }
}