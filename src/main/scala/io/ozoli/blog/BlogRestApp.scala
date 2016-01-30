package io.ozoli.blog

import akka.actor.{ActorSystem, Props}
import akka.io.IO
import grizzled.slf4j.Logger
import io.ozoli.db.DatabaseConfiguration
import spray.can.Http

/**
 * Blog REST application with start and stop logic and declaring the Akka Actor system.
 */
class BlogRestApp extends DatabaseConfiguration {

  private val applicationName = "BlogRestApp"

  implicit val actorSystem = ActorSystem(s"$applicationName-system")

  override val logger = Logger[this.type]

  def start() {
    logger.info(s"Starting $applicationName Service")

    // the handler actor replies to incoming HttpRequests
    val blogService = actorSystem.actorOf(Props[BlogService], name = "BlogService")

    IO(Http) ! Http.Bind(blogService, interface = conf.getString("blog.hostname"),
      port = conf.getInt("blog.port"))
  }

  def stop() {
    logger.info(s"Stopping $applicationName Service")
    actorSystem.shutdown()
  }
}
