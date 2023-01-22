lazy val commonSettings = Seq(
  version := "0.0.1",
  scalaVersion := "3.2.1",
)

lazy val testSettings = Seq(
  libraryDependencies += "org.scalatest" %% "scalatest" % "3.2.15" % Test,
)

lazy val root = (project in file(".")).
  aggregate(core).
  settings(commonSettings: _*).
  settings(
    name := "root"
  )

lazy val api = (project in file("core") / "api")
  .settings(commonSettings: _*)
  .settings(
    Compile / PB.protoSources := Seq(baseDirectory.value / "src" / "main" / "protobuf"),
    Compile / PB.targets := Seq(
      scalapb.gen() -> (Compile / sourceManaged).value / "scalapb"
    )
  )

lazy val processing = (project in file("core") / "processing")
  .settings(commonSettings: _*)
  .settings(testSettings: _*)
  .dependsOn(api)

lazy val core = (project in file("core")).
  dependsOn(api, processing).
  settings(commonSettings: _*).
  settings(
    name := "core",
  )
