lazy val commonSettings = Seq(
  version := "0.0.1",
  scalaVersion := "3.2.1"
)

lazy val testSettings = Seq(
  libraryDependencies += "org.scalatest" %% "scalatest" % "3.2.15" % Test
)

lazy val root = (project in file("."))
  .aggregate(core)
  .settings(commonSettings: _*)
  .settings(
    name := "root"
  )

lazy val api = (project in file("core") / "api")
  .settings(commonSettings: _*)
  .settings(
    Compile / PB.protoSources := Seq(
      baseDirectory.value / "src" / "main" / "protobuf"
    ),
    Compile / PB.targets := Seq(
      scalapb.gen() -> (Compile / sourceManaged).value / "scalapb"
    )
  )
  .settings(
    libraryDependencies ++= Seq(
      "com.thesamet.scalapb" %% "scalapb-runtime" % scalapb.compiler.Version.scalapbVersion % "protobuf"
    )
  )

lazy val services = (project in file("core") / "services")
  .settings(commonSettings: _*)
  .settings(testSettings: _*)
  .dependsOn(api)

lazy val processing = (project in file("core") / "processing")
  .settings(commonSettings: _*)
  .settings(testSettings: _*)
  .settings(
    libraryDependencies += "com.google.transit" % "gtfs-realtime-bindings" % "0.0.4"
  )
  .dependsOn(api, services)

lazy val core = (project in file("core"))
  .aggregate(api, processing, services)
  .dependsOn(api, processing, services)
  .settings(commonSettings: _*)
  .settings(testSettings: _*)
  .settings(
    name := "core"
  )
