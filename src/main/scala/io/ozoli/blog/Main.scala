package io.ozoli.blog

import akka.actor.{ActorSystem, Props}
import akka.io.IO
import io.ozoli.blog.database.DatabaseConfiguration
import spray.can.Http

object Main extends App with DatabaseConfiguration {

  implicit val system = ActorSystem()

  // the handler actor replies to incoming HttpRequests
  val handler = system.actorOf(Props[DemoService], name = "handler")

  IO(Http) ! Http.Bind(handler, interface = "localhost", port = 8080)
}
