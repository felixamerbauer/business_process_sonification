name := "business_process_sonification"

version := "1.0.5"

scalaVersion := "2.11.8"

scalacOptions ++= Seq(
	"-encoding", "UTF-8", 
	"-deprecation", 
	"-unchecked", 
	"-feature", 
	"-language:implicitConversions", 
	"-language:postfixOps", 
	"-language:existentials", 
	"-language:experimental.macros",
	"-target:jvm-1.7")

javacOptions ++= Seq(
	"-source", "1.7",
	"-target", "1.7")

// SBT-Eclipse settings
// In order to avoid Eclipse and sbt working on the same files: At least in theory there could be race conditions and such
EclipseKeys.eclipseOutput := Some(".target")
EclipseKeys.executionEnvironment := Some(EclipseExecutionEnvironment.JavaSE17)
EclipseKeys.withSource := true
EclipseKeys.createSrc := EclipseCreateSrc.Default + EclipseCreateSrc.Resource

// dependencies
libraryDependencies ++= Seq(
	"org.scalatest" %% "scalatest" % "2.2.6" % "test->default",
	"com.sksamuel.diff" % "diff" % "1.1.11" % "test->default",
	"com.typesafe.scala-logging" %% "scala-logging" % "3.4.0",
	"org.slf4j" % "slf4j-log4j12" % "1.7.21",
 	"org.apache.directory.studio" % "org.apache.commons.io" % "2.4",
	"io.dropwizard.metrics" % "metrics-core" % "3.1.2",
	"com.google.guava" % "guava" % "16.0.1",
	"org.scala-lang.modules" %% "scala-xml" % "1.0.5")

// assembly
jarName in assembly := s"sonification_${version.value}.jar"
test in assembly := {}
mainClass in assembly := Some("sonification.StandaloneRunner")
mergeStrategy in assembly <<= (mergeStrategy in assembly) { (old) =>
  {
    case PathList(ps @ _*) if ps.last endsWith ".xes" => MergeStrategy.first
    case PathList(ps @ _*) if ps.last endsWith ".png" => MergeStrategy.first
    case x => old(x)
  }
}

// custom tasks
lazy val copyToPackageProject = taskKey[Unit]("Copies the packaged JAR to the ProM project")

copyToPackageProject := {
    val bin: File = (packageBin in Compile).value
    val targets = Seq(new File("../business_process_sonification_package/lib/business-process-sonification-core.jar"))
    println(s"Copying $bin to ${targets.mkString("\n","\n","")}")
    targets.foreach { target =>
   		IO.copyFile(bin, target)
   	}
}