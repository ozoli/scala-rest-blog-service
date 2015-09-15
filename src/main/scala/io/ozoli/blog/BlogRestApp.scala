package io.ozoli.blog

import akka.actor.{ActorSystem, Props}
import akka.io.IO
import grizzled.slf4j.Logger
import io.ozoli.blog.database.DatabaseConfiguration
import spray.can.Http
import io.ozoli.blog.util.ApplicationLifecycle

class BlogRestApp extends ApplicationLifecycle with DatabaseConfiguration {

  private[this] var started: Boolean = false

  private val applicationName = "BlogRestApp"

  implicit val actorSystem = ActorSystem(s"$applicationName-system")

  val logger = Logger[this.type]

  def start() {
    logger.info(s"Starting $applicationName Service")

    if (!started) {
      started = true

      // the handler actor replies to incoming HttpRequests
      val blogService = actorSystem.actorOf(Props[BlogService], name = "BlogService")

      IO(Http) ! Http.Bind(blogService, interface = conf.getString("blog.app.hostname"),
        port = conf.getInt("blog.app.port"))
    }
  }

  def stop() {
    logger.info(s"Stopping $applicationName Service")

    if (started) {
      started = false
      actorSystem.shutdown()
    }
  }
}
