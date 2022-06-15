
ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "3.1.2"

val unitTesting = Seq(
    "org.scalactic" %% "scalactic" % "3.2.12",
    "org.scalatest" %% "scalatest" % "3.2.12" % "test"
)

val logging = Seq(
    "ch.qos.logback" % "logback-classic" % "1.2.11",
    "com.typesafe.scala-logging" %% "scala-logging" % "3.9.5"
)

val databaseDrivers = Seq(
    "org.mariadb.jdbc" % "mariadb-java-client" % "3.0.5",
    "org.mongodb.scala" % "mongo-scala-driver_2.13" % "4.6.0",
    "org.mongodb.scala" % "mongo-scala-bson_2.13" % "4.6.0"
)

val jbAnnotations = "org.jetbrains" % "annotations" % "23.0.0"
val json = "com.github.losizm" %% "little-json" % "9.0.0"

libraryDependencies ++= unitTesting
libraryDependencies ++= logging
libraryDependencies += jbAnnotations

libraryDependencies ++= databaseDrivers
libraryDependencies += json

libraryDependencies += "com.github.jwt-scala" %% "jwt-core" % "9.0.5"

libraryDependencies += "javax.servlet" % "javax.servlet-api" % "4.0.1" % "provided"

enablePlugins(TomcatPlugin)
containerPort := 8084

lazy val root = (project in file("."))
  .settings(
      name := "minesweeper",
      idePackagePrefix := Some("edu.mmsa.danikvitek.minesweeper")
  )
