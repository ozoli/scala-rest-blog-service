package io.ozoli.blog.database

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

  val blogEntries = TableQuery[BlogEntries]

  val db = Database.forConfig("mysql")

  // Insert the current blog entries
  val currentBlogEntries : Seq[BlogEntry] =
    RssReader.extractRss("http://feeds.feedburner.com/OliverCarrsBlog?fmt=xml")

  try {
    val insertAction: DBIO[Option[Int]] = blogEntries ++= currentBlogEntries

    val setupAction: DBIO[Unit] = DBIO.seq(
      // Create the schema for the DDL for BlogEntries
      // tables using the query interfaces
      blogEntries.schema.drop,
      blogEntries.schema.create)

    val combinedAction: dbio.DBIOAction[Option[Int], NoStream, All with All] =
      setupAction >> insertAction

    val combinedFuture: Future[Option[Int]] =
      {
        println("loaded blog entries")
        println(currentBlogEntries.head.id)
        println(currentBlogEntries.head.title)
        println(currentBlogEntries.size)
        db.run(combinedAction)
      }

    Await.result(combinedFuture, Duration.Inf)

  } finally db.close()
}
