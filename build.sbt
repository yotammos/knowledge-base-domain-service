name := "knowledge-base-domain-service"

version := "0.1"

scalaVersion := "2.12.8"

resolvers ++= Seq(
  "Twitter repository" at "http://maven.twttr.com"
)

lazy val finagleVersion = "19.12.0"
lazy val circeVersion = "0.12.3"

libraryDependencies ++= Seq(
  "com.twitter" %% "finagle-http" % finagleVersion,
  "com.twitter" %% "finagle-mysql" % finagleVersion,
  "com.twitter" %% "finagle-thrift" % finagleVersion,
  "com.twitter" %% "scrooge-core" % finagleVersion,
  "io.circe" %% "circe-core" % circeVersion,
  "io.circe" %% "circe-generic" % circeVersion,
  "io.circe" %% "circe-parser" % circeVersion,
  "org.scalatest" %% "scalatest" % "3.1.0" % "test"
)

com.twitter.scrooge.ScroogeSBT.newSettings