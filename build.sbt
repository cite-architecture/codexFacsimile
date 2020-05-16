//name := "OHCO2 text library"

// XML libraries moved in 2.11, so can't support 2.10.
// Airframe logging only for >= 2.12
lazy val supportedScalaVersions = List("2.12.4") //List("2.11.8", "2.12.4")

lazy val rootProject = project.settings(  // your existing library
  name := "codexFacsimile",
  organization := "edu.holycross.shot",
  version := "0.0.1",
  licenses += ("GPL-3.0",
    url("https://opensource.org/licenses/gpl-3.0.html")),
  resolvers += Resolver.jcenterRepo,
  resolvers += Resolver.bintrayRepo("neelsmith", "maven"),
  libraryDependencies ++= Seq(
    "org.scalatest" %% "scalatest" % "3.0.1" % "test",
    "org.wvlet.airframe" %% "airframe-log" % "19.8.10",

    "edu.holycross.shot.cite" %% "xcite" % "4.1.1"
  )
)

lazy val docs = project       // new documentation project
  .in(file("mdocs")) // important: it must not be docs/
  .dependsOn(rootProject)
  .enablePlugins(MdocPlugin)
  .settings(
    mdocIn := file("guide"),
    mdocOut := file("docs")
  )

/*

    */
