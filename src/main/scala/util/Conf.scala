package util

import com.typesafe.config.{Config, ConfigFactory}
import akka.actor.{ExtendedActorSystem, ExtensionIdProvider, ExtensionId, Extension}

class ConfExtensionImpl(config: Config) extends Extension {
  config.checkValid(ConfigFactory.defaultReference)

  val appHostName = config.getString("ozoli-blog.app.hostname")
  val appPort = config.getInt("ozoli-blog.app.port")

  val dbUsername = config.getString("ozoli-blog.db.username")
  val dbPassword = config.getString("ozoli-blog.db.password")
  val dbPort = config.getInt("ozoli-blog.db.port")
  val dbName = config.getString("ozoli-blog.db.name")

  val dbPoolMaxObjects = config.getInt("ozoli-blog.db.pool.maxObjects")
  val dbPoolMaxIdle = config.getInt("ozoli-blog.db.pool.maxIdle")
  val dbPoolMaxQueueSize = config.getInt("ozoli-blog.db.pool.maxQueueSize")
}

object ConfExtension extends ExtensionId[ConfExtensionImpl] with ExtensionIdProvider {
  def lookup() = ConfExtension

  def createExtension(system: ExtendedActorSystem) = new ConfExtensionImpl(system.settings.config)
}
