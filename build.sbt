name := "feast-bot"

version := "1.0"

scalaVersion := "2.11.7"

libraryDependencies ++= Seq(
  "org.postgresql" % "postgresql" % "9.4.1210.jre7",
  "mysql" % "mysql-connector-java" % "5.1.36",
  "com.typesafe.akka" %% "akka-actor" % "2.4.4",
  "com.typesafe.akka" %% "akka-stream" % "2.4.4",
  "com.typesafe.akka" %% "akka-http-experimental" % "2.4.4",
  "org.json4s" %% "json4s-native" % "3.3.0",
  "com.typesafe.slick" %% "slick-hikaricp" % "3.1.1",
  "ch.qos.logback" % "logback-classic" % "1.1.3",
  "com.typesafe.slick" %% "slick" % "3.1.1",
  "org.scalatest" %% "scalatest" % "2.2.5" % "test",
  "com.h2database" % "h2" % "1.4.187",
  "com.typesafe.akka" %% "akka-http-testkit" % "2.4.4" % "test"
)
