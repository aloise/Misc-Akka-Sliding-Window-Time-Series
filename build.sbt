name := "FyberAssignment"

version := "1.0"

scalaVersion := "2.11.8"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-stream" % "2.4.6",
  "com.typesafe" % "config" % "1.3.0",
  "org.scalatest" % "scalatest_2.11" % "2.2.6" % Test
)