name := "ozoli-blog-service"

version := "1.0"

scalaVersion := "2.11.7"

resolvers += "spray repo" at "http://repo.spray.io"

val sprayVersion = "1.3.2"
val akkaVersion = "2.3.6"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % akkaVersion
  ,"io.spray" %% "spray-can" % sprayVersion
  ,"io.spray" %% "spray-routing" % sprayVersion
  ,"org.json4s" %% "json4s-native" % "3.2.11"
  // -- Slick --
  ,"com.typesafe.slick" %% "slick" % "3.0.2"
  ,"com.github.tototoshi" %% "slick-joda-mapper" % "2.0.0"

  ,"com.github.mauricio" %% "mysql-async" % "0.2.18"

  , "mysql" % "mysql-connector-java" % "5.1.35"
  ,"com.zaxxer" % "HikariCP-java6" % "2.3.2"
)

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-testkit" % akkaVersion % "test",
  "io.spray" %% "spray-testkit" % sprayVersion % "test",
  "org.scalatest" %% "scalatest" % "2.1.4" % "test"
)