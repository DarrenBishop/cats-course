name := "cats"

version := "0.1.0"

scalaVersion := "2.13.13"

val catsVersion = "2.10.0"

libraryDependencies ++= Seq(
  "org.typelevel" %% "cats-core" % catsVersion,
)

scalacOptions ++= Seq(
  "-language:higherKinds"
)
