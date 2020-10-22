name := "Scala_Doopay"

version := "0.1"

scalaVersion := "2.13.3"

fork := true

ThisBuild / useCoursier := false

javacOptions in (Compile, compile) ++= Seq("-source", "1.8", "-target", "1.8", "-g:lines")

mainClass in (Compile,run) := Some("Driver")  // specifying main class

assemblyMergeStrategy in assembly := {
  case PathList("META-INF", xs @ _*) => MergeStrategy.discard
  case x => MergeStrategy.first
}

libraryDependencies ++= Seq(
  "org.scala-lang.modules" %% "scala-xml" % "1.3.0", // xml
  "org.scala-lang.modules" %% "scala-parser-combinators" % "1.1.2",
  "org.scala-lang" % "scala-reflect" % scalaVersion.value % Provided,
  "com.typesafe" % "config" % "1.4.0", //Typesafe configuration
  "ch.qos.logback" % "logback-classic" % "1.2.3", // Logback logging framework
  "com.typesafe.scala-logging" %% "scala-logging" % "3.9.2",
  "org.gnieh" % "logback-config" % "0.3.1",
  "junit" % "junit" % "4.13", // JUnit Testing Framework
  "org.apache.hadoop" % "hadoop-hdfs" % "3.3.0", // Hadoop
  "org.apache.hadoop" % "hadoop-client" % "3.3.0",
  "org.apache.hadoop" % "hadoop-mapreduce-client-core" % "3.3.0"
)

