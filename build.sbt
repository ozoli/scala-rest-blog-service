name := "blogRestService"

version := "1.0"

scalaVersion := "2.11.7"

scalacOptions ++= Seq("-optimize", "-language:postfixOps")

resolvers += "spray repo" at "http://repo.spray.io"

val sprayVersion = "1.3.3"
val akkaVersion = "2.3.9"

parallelExecution in Test := false

parallelExecution in jacoco.Config := false

jacoco.excludes        in jacoco.Config := Seq("io.ozoli.blog.BlogService.**anonfun**")

javacOptions ++= Seq("-source", "1.8", "-target", "1.8", "-Xlint")

jacoco.settings

initialize := {
  val _ = initialize.value
  if (sys.props("java.specification.version") != "1.8")
    sys.error("Java 8 is required for this project.")
}

// sbt-pack: JAR assembly, automatically find def main(args:Array[String]) methods from classpath
packAutoSettings

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % akkaVersion
  ,"io.spray" %% "spray-can" % sprayVersion
  ,"io.spray" %% "spray-json" % "1.3.2"
  ,"io.spray" %% "spray-routing" % sprayVersion
  ,"org.json4s" %% "json4s-native" % "3.2.11"
  ,"com.typesafe" % "config" % "1.3.0"
)

// -- MongoDB --
libraryDependencies ++= Seq(
  "org.mongodb" % "mongodb-driver" % "3.2.0"
  ,"org.mongodb.scala" %% "mongo-scala-driver" % "1.1.0"
)

// -- Logging --
libraryDependencies ++= Seq(
  "ch.qos.logback" % "logback-classic" % "1.1.3"
  ,"org.slf4j" % "slf4j-api" % "1.7.5"
  ,"org.clapper" %% "grizzled-slf4j" % "1.0.2"
)

// Test dependencies
libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-testkit" % akkaVersion % "test"
  ,"io.spray" %% "spray-testkit" % sprayVersion % "test"
  ,"org.scalatest" %% "scalatest" % "2.2.6" % "test"
  ,"com.github.simplyscala" %% "scalatest-embedmongo" % "0.2.2" % "test"
  ,"com.typesafe.akka" %% "akka-http-experimental" % "1.0-M3" % "test"
)
