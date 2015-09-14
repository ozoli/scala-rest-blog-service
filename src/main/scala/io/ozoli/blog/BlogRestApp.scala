package io.ozoli.blog

import akka.actor.{ActorSystem, Props}
import akka.io.IO
import io.ozoli.blog.database.DatabaseConfiguration
import spray.can.Http

object BlogRestApp extends App with DatabaseConfiguration {

  implicit val system = ActorSystem()

  // the handler actor replies to incoming HttpRequests
  val handler = system.actorOf(Props[BlogService], name = "handler")

  IO(Http) ! Http.Bind(handler, interface = conf.getString("blog.app.hostname"), port = conf.getInt("blog.app.port"))
}
