//name := "OHCO2 text library"

// XML libraries moved in 2.11, so can't support 2.10.
// Airframe logging only for >= 2.12
lazy val supportedScalaVersions = List("2.12.10") //List("2.11.8", "2.12.4")

lazy val root = (project in file(".")).
aggregate(crossed.js, crossed.jvm).
settings(
  // crossScalaVersions must be set to Nil on the aggregating project
  crossScalaVersions := Nil,
  publish / skip := true
)

lazy val crossed = crossProject(JSPlatform, JVMPlatform).in(file(".")).
  settings(  // your existing library
  name := "codexfax",
  organization := "edu.holycross.shot",
  version := "2.1.0",
  licenses += ("GPL-3.0",
    url("https://opensource.org/licenses/gpl-3.0.html")),
  resolvers += Resolver.jcenterRepo,
  resolvers += Resolver.bintrayRepo("neelsmith", "maven"),

  libraryDependencies ++= Seq(
    "org.scalatest" %% "scalatest" % "3.1.2" % "test",
    "org.wvlet.airframe" %% "airframe-log" % "20.5.2",

    "edu.holycross.shot.cite" %% "xcite" % "4.3.0",
    "edu.holycross.shot" %% "citeobj" % "7.4.0",
    "edu.holycross.shot" %% "citebinaryimage" % "3.1.1"

  )
).
jvmSettings(
  // JVM-specific settings:
  libraryDependencies ++= Seq(
    "org.scala-js" %% "scalajs-stubs" % "1.0.0" % "provided",
  )

).
jsSettings(
  // JS-specific settings:
  scalaJSUseMainModuleInitializer := true,
)



lazy val docs = project       // new documentation project
  .in(file("mdocsrc")) // important: it must not be docs/
  .dependsOn(crossed.jvm)
  .enablePlugins(MdocPlugin)
  .settings(
    mdocIn := file("mdocs/guide"),
    mdocOut := file("docs")
  )
