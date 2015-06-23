import sbt._
import sbt.Keys._

object FSPExBuild extends Build {

  val scalaOptions = Seq(
        "-deprecation",
        "-unchecked",
        "-Yclosure-elim",
        "-Yinline-warnings",
        "-optimize",
        "-language:implicitConversions",
        "-language:postfixOps",
        "-language:existentials",
        "-feature"
  )

  val key = AttributeKey[Boolean]("javaOptionsPatched")

  val updateTask = TaskKey[Unit]("update-fspex")

  val updateSettings = Seq(
    updateTask <<= (copyResources in Compile, compile in Compile) map {
      (c, p) =>
    }
  )


  lazy val root = 
    Project("root", file(".")).settings(
      organization := "fr.laas.fspex",
      name := "FSPEx",
      version := "0.1.0-SNAPSHOT",
      scalaVersion := "2.10.3",
     
      updateTask <<= (copyResources in Compile, compile in Compile) map {
        (c, p) =>
      },
 
      scalacOptions ++= scalaOptions,
      scalacOptions in Compile in doc ++= Seq("-diagrams", "-implicits"),
      parallelExecution := false,

      fork in run := true,

      mainClass := Some("laas.fspex.Main"),

      javaOptions in (Compile,run) ++= Seq("-Xmx6G"),

      /*libraryDependencies ++= Seq(
        "org.scalatest" %% "scalatest" % "2.0.M5b" % "test",
        "com.google.guava" % "guava" % "14.0.1"
      ),*/

    resolvers ++= Seq(
      //"opengeo" at "http://repo.opengeo.org/",
      Resolver.sonatypeRepo("snapshots")),
    
      licenses := Seq("MIT License" -> url("http://www.opensource.org/licenses/mit-license.html")),
      homepage := Some(url("http://www.homepages.laas.fr/umaivodj"))
    )
}
