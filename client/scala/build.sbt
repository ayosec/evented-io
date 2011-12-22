
import AssemblyKeys._

name := "client-eio"

version := "0.1"

scalaVersion := "2.9.1"

scalacOptions ++= Seq("-deprecation", "-optimise", "-unchecked")

mainClass := Some("com.ayosec.eioclient.Runner")

fork in run := true

resolvers += "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/"

libraryDependencies ++= Seq(
  "joda-time" % "joda-time" % "1.2.1",
  "com.ning" % "async-http-client" % "1.6.5",
  "org.slf4j" % "slf4j-nop" % "1.6.2",
  "net.sf.trove4j" % "trove4j" % "3.0.1",
  "net.liftweb" %% "lift-json" % "2.4-M4",
  "org.scalatest" %% "scalatest" % "1.6.1" % "test"
)


seq(assemblySettings: _*)

assembleArtifact in packageScala := false


// vim: syntax=scala
