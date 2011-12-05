
name := "client-eio"

version := "0.1"

scalaVersion := "2.9.1"

mainClass := Some("com.ayosec.eioclient.Runner")

resolvers += "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/"

libraryDependencies += "com.ning" % "async-http-client" % "1.6.5"

//libraryDependencies += "se.scalablesolutions.akka" % "akka-actor" % "1.2"

libraryDependencies += "net.liftweb" %% "lift-json" % "2.4-M4"

seq(com.github.retronym.SbtOneJar.oneJarSettings: _*)

scalacOptions += "-deprecation"

// vim: syntax=scala
