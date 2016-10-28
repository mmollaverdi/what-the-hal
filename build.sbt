scalaVersion := "2.11.8"

sbtVersion := "0.13.11"

name := "what-the-hal"

resolvers ++= Seq(
  Resolver.sonatypeRepo("releases"),
  Resolver.sonatypeRepo("snapshots")
)


libraryDependencies ++= Seq(
  "com.chuusai" %% "shapeless"            % "2.3.2",
  "io.argonaut" %% "argonaut"             % "6.2-M3",
  "org.specs2"  %% "specs2-core"          % "3.8.5" % "test",
  "org.specs2"  %% "specs2-matcher-extra" % "3.8.5" % "test"
)

scalacOptions ++= Seq(
  "-deprecation",
  "-unchecked",
  "-feature",
  "-Xlint",
  "-language:higherKinds"
)
