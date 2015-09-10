package io.ozoli.blog.database

import com.typesafe.config.ConfigFactory
import io.ozoli.blog.{RssReader, BlogEntry, BlogEntries}

import slick.dbio
import slick.dbio.Effect.All
import slick.lifted.TableQuery

import slick.driver.MySQLDriver.api._

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}

/**
 * Created by ocarr on 01/09/15.
 */
trait DatabaseConfiguration {
  var conf = ConfigFactory.load

  val blogEntries = TableQuery[BlogEntries]

  val db = Database.forConfig("blog.db")

  // Insert the current blog entries
  lazy val currentBlogEntries : Seq[BlogEntry] = RssReader.extractRss(conf.getString("blog.rss.uri"))

  try {
    val insertAction: DBIO[Option[Int]] = blogEntries ++= currentBlogEntries

    // Create the schema for the DDL for BlogEntries tables using the query interfaces
    val setupAction: DBIO[Unit] = DBIO.seq(blogEntries.schema.drop, blogEntries.schema.create)

    val combinedAction: dbio.DBIOAction[Option[Int], NoStream, All with All] = setupAction >> insertAction

    val combinedFuture: Future[Option[Int]] = db.run(combinedAction)

    Await.result(combinedFuture, Duration.Inf)

  } finally db.close()
}
