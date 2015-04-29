    ///////////////////////////////////////////////////////////////////////////
   //
  //
 //   Copyright 2015 Max Amanshauser <max@lambdalifting.org>
///////////////////////////////////////////////////////////////////////////////


//_* Declarations ======================================================
name := "tiabot"

version := "0.0-SNAPSHOT"

scalaVersion := "2.11.5"

resolvers += "Scalaz Bintray Repo" at "http://dl.bintray.com/scalaz/releases"

// core
libraryDependencies += "org.scalaz" %% "scalaz-core" % "7.1.1"

libraryDependencies += "org.scalaz" %% "scalaz-effect" % "7.1.1"

libraryDependencies += "org.scalaz" %% "scalaz-concurrent" % "7.1.1"

libraryDependencies += "org.http4s" %% "http4s-blazeserver" % "0.6.1"

libraryDependencies += "org.http4s" %% "http4s-dsl" % "0.6.1"

libraryDependencies += "org.http4s" %% "http4s-blazeclient" % "0.6.1"

libraryDependencies += "io.argonaut" %% "argonaut" % "6.1-M4"

libraryDependencies += "org.specs2" %% "specs2" % "2.4.15" % "test"

//_* Settings ======================================================----
scalaSource in Compile <<= baseDirectory(_ / "src")

scalaSource in Test <<= baseDirectory(_ / "test")

resourceDirectory in Compile <<= baseDirectory(_ / "resources")

test in assembly := {}

enablePlugins(JavaAppPackaging)
