scalaVersion := "2.11.8"

sbtVersion := "0.13.11"

name := "what-the-hal"

resolvers ++= Seq(
  Resolver.sonatypeRepo("releases"),
  Resolver.sonatypeRepo("snapshots")
)

libraryDependencies ++= Seq(
  "com.chuusai" %% "shapeless" % "2.3.2",
  "io.argonaut" %% "argonaut"  % "6.1" 
)

scalacOptions ++= Seq(
  "-deprecation",
  "-unchecked",
  "-feature",
  "-Xlint",
  "-language:higherKinds"
)
