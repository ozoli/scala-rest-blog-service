package io.ozoli.db

import java.net.URL

import com.typesafe.config.ConfigFactory
import io.ozoli.blog.domain.{BlogEntries, BlogEntry}
import io.ozoli.blog.util.RssReader
import slick.driver.MySQLDriver.api._
import slick.jdbc.meta.MTable
import slick.lifted.TableQuery

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.concurrent.{Await, Future}


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
    val tablesExist: DBIO[Boolean] = MTable.getTables.map { tables =>
      val names = Vector(blogEntries.baseTableRow.tableName)
      names.intersect(tables.map(_.name.name)) == names
    }

    // Create the schema for the DDL for BlogEntries tables using the query interfaces
    val createAction: DBIO[Unit] = DBIO.seq(blogEntries.schema.create)
    val dropCreateAction: DBIO[Unit] = DBIO.seq(blogEntries.schema.drop, blogEntries.schema.create)

    val createIfNotExist: DBIO[Unit] = tablesExist.flatMap(exist => if (!exist) createAction else dropCreateAction)

    val insertAction: DBIO[Option[Int]] = blogEntries ++= currentBlogEntries

    val combinedFuture: Future[Option[Int]] = db.run(createIfNotExist >> insertAction)

    Await.result(combinedFuture, 100 seconds)

  } finally db.close()
}
