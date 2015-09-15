package io.ozoli.blog.util

import com.typesafe.config.{Config, ConfigFactory}
import akka.actor.{ExtendedActorSystem, ExtensionIdProvider, ExtensionId, Extension}

class ConfExtensionImpl(config: Config) extends Extension {
  config.checkValid(ConfigFactory.defaultReference)

  val appHostName = config.getString("blog.app.hostname")
  val appPort = config.getInt("blog.app.port")

  val dbUsername = config.getString("blog.db.user")
  val dbPassword = config.getString("blog.db.password")
  val dbPort = config.getInt("blog.db.port")
  val dbName = config.getString("blog.db.name")

  val dbPoolMaxObjects = config.getInt("blog.db.pool.maxObjects")
  val dbPoolMaxIdle = config.getInt("blog.db.pool.maxIdle")
  val dbPoolMaxQueueSize = config.getInt("blog.db.pool.maxQueueSize")
}

object ConfExtension extends ExtensionId[ConfExtensionImpl] with ExtensionIdProvider {
  def lookup() = ConfExtension

  def createExtension(system: ExtendedActorSystem) = new ConfExtensionImpl(system.settings.config)
}
