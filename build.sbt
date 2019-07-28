val Specs2Version = "4.6.0"
val ArrowVersion  = "0.14.1"

resolvers += Resolver.sonatypeRepo("releases")
resolvers += Resolver.sonatypeRepo("snapshots")

lazy val root = (project in file("."))
  .settings(
    organization := "Neurodyne",
    name := "arrow",
    version := "0.0.1",
    scalaVersion := "2.12.8",
    maxErrors := 3,
    libraryDependencies ++= Seq(
      "org.specs2"       %% "specs2-core"    % Specs2Version % "test",
      "org.apache.arrow" % "arrow-java-root" % ArrowVersion,
      "org.apache.arrow" % "arrow-memory"    % ArrowVersion,
      "org.apache.arrow" % "arrow-vector"    % ArrowVersion,
      "org.apache.arrow" % "arrow-tools"     % ArrowVersion
    )
  )

// Refine scalac params from tpolecat
scalacOptions --= Seq(
  "-Xfatal-warnings"
)

addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.1" cross CrossVersion.full)

addCommandAlias("fmt", "all scalafmtSbt scalafmt test:scalafmt")
addCommandAlias("chk", "all scalafmtSbtCheck scalafmtCheck test:scalafmtCheck")
