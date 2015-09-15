package io.ozoli.blog.database

import java.net.URL

import com.typesafe.config.ConfigFactory
import io.ozoli.blog.domain.{BlogEntries, BlogEntry}
import io.ozoli.blog.util.RssReader

import scala.concurrent.{Await, Future}
import scala.concurrent.duration._
import slick.driver.MySQLDriver.api._
import slick.lifted.TableQuery


/**
 * Here the current Blog Entries are read from the Feed URL
 * and any previous Blog Entries in the database are replaced with the new entries.
 */
trait DatabaseConfiguration {
  lazy val conf = ConfigFactory.load

  val blogEntries = TableQuery[BlogEntries]

  val db = Database.forConfig("blog.db")

  // Read the current blog entries from the Feed URL
  lazy val currentBlogEntries : Seq[BlogEntry] = RssReader.extractRss(new URL(conf.getString("blog.rss.uri")))

  try {
    val insertAction: DBIO[Option[Int]] = blogEntries ++= currentBlogEntries

    // Create the schema for the DDL for BlogEntries tables using the query interfaces
    val setupAction: DBIO[Unit] = DBIO.seq(blogEntries.schema.drop, blogEntries.schema.create)

    val combinedFuture: Future[Option[Int]] = db.run(setupAction >> insertAction)

    Await.result(combinedFuture, 100 seconds)

  } finally db.close()
}
