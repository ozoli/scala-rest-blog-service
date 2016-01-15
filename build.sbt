name := "blogRestService"

assemblyJarName in assembly := "blogRestService.jar"

version := "1.0"

scalaVersion := "2.11.7"
scalacOptions ++= Seq("-optimize", "-language:postfixOps")

resolvers += "spray repo" at "http://repo.spray.io"

val sprayVersion = "1.3.2"
val akkaVersion = "2.3.6"

parallelExecution in Test := false

javacOptions ++= Seq("-source", "1.8", "-target", "1.8", "-Xlint")

initialize := {
  val _ = initialize.value
  if (sys.props("java.specification.version") != "1.8")
    sys.error("Java 8 is required for this project.")
}

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % akkaVersion
  ,"io.spray" %% "spray-can" % sprayVersion
  ,"io.spray" %% "spray-json" % sprayVersion
  ,"io.spray" %% "spray-routing" % sprayVersion
  ,"org.json4s" %% "json4s-native" % "3.2.11"

  // -- MongoDB --
  ,"org.mongodb" % "mongodb-driver" % "3.2.0"
  ,"org.mongodb.scala" %% "mongo-scala-driver" % "1.1.0"

  // -- Logging --
  ,"ch.qos.logback" % "logback-classic" % "1.1.3"
  ,"org.slf4j" % "slf4j-api" % "1.7.5"
  ,"org.clapper" %% "grizzled-slf4j" % "1.0.2"
  ,"commons-daemon" % "commons-daemon" % "1.0.15"

  // Test dependencies
  ,"com.typesafe.akka" %% "akka-testkit" % akkaVersion % "test"
  ,"io.spray" %% "spray-testkit" % sprayVersion % "test"
  ,"org.scalatest" %% "scalatest" % "2.2.6" % "test"
  ,"com.github.simplyscala" %% "scalatest-embedmongo" % "0.2.2" % "test"
)
