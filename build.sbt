val sparkVersion = "2.3.1"

lazy val commonSettings = Seq(
  organization := "com.chrisluttazi.spark.etl",
  version := "0.1.0-SNAPSHOT",
  scalaVersion := "2.11.12",
  resolvers ++= Seq(
    "Artima Maven Repository" at "https://repo.artima.com/releases"
  ),
  libraryDependencies ++= Seq(
    "org.apache.spark" %% "spark-core" % sparkVersion,
    "org.apache.spark" %% "spark-sql" % sparkVersion,
    "org.apache.spark" %% "spark-hive" % sparkVersion,
    "org.scalactic" %% "scalactic" % "3.0.1",
    "org.scalatest" %% "scalatest" % "3.0.1" % "test"
  )
)

lazy val commons = project
  .settings(
    commonSettings
    // other settings
  )

lazy val root = (project in file(".")).
  aggregate(commons)