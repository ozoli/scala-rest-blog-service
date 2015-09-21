package db

import com.github.mauricio.async.db.Configuration
import com.github.mauricio.async.db.mysql.pool.MySQLConnectionFactory
import com.github.mauricio.async.db.pool.ConnectionPool
import com.github.mauricio.async.db.pool.PoolConfiguration
import io.ozoli.blog.util.ConfExtension
import akka.actor.ActorSystem

class Pool(system: ActorSystem) {
  val conf = ConfExtension(system)

  val configuration = new Configuration(username = conf.dbUsername,
    host = conf.dbHost,
    port = conf.dbPort,
    password = Some(conf.dbPassword),
    database = Some(conf.dbName))

  val factory = new MySQLConnectionFactory(configuration)
  val pool = new ConnectionPool(factory,
    new PoolConfiguration(conf.dbPoolMaxObjects, conf.dbPoolMaxIdle, conf.dbPoolMaxQueueSize))
}
