package io.ozoli.db

import grizzled.slf4j.Logger

import concurrent.ExecutionContext.Implicits.global

import java.net.URL
import java.time.format.DateTimeFormatter

import com.typesafe.config.ConfigFactory
import io.ozoli.blog.domain.BlogEntry
import io.ozoli.blog.util.RssReader

import org.mongodb.scala.{MongoCollection, MongoDatabase, MongoClient, Document}

/**
 * Here the current Blog Entries are read from the Feed URL
 * and any previous Blog Entries in the database are replaced with the new entries.
 */
trait DatabaseConfiguration {
  val conf = ConfigFactory.load

  val logger = Logger[this.type]

  protected var mongoClient: MongoClient = MongoClient(conf.getString("blog.db.uri"))

  def getMongoClient : MongoClient = mongoClient

  var database: MongoDatabase = getMongoClient.getDatabase(conf.getString("blog.db.name"))

  var blogCollection: MongoCollection[Document] = database.getCollection(conf.getString("blog.db.collectionName"))

  // Read the current blog entries from the Feed URL
  lazy val currentBlogEntries : Seq[BlogEntry] = RssReader.extractRss(new URL(conf.getString("blog.rss.uri")))

  // Insert all the BlogEntries read from RSS
  val documents = currentBlogEntries map { blogEntry: BlogEntry => Document(
    "id" -> blogEntry.id.toString,
    "pubDate" -> DateTimeFormatter.ISO_DATE_TIME.format(blogEntry.pubDate),
    "title" -> blogEntry.title,
    "linkTitle" -> blogEntry.linkTitle,
    "body" -> blogEntry.body,
    "category" -> blogEntry.category) }

  val insertAndCount = for {
    dropCollectionFuture <- database.getCollection("blogs").drop().toFuture()
    createCollectionFutuere <- database.createCollection("blogs").toFuture()
    insertResult <- blogCollection.insertMany(documents.toList).toFuture()
    countResult <- blogCollection.count().toFuture()
  } yield {
      logger.info(s"total # of documents after inserting BlogEntries from RSS: $countResult")
  }
}
