package io.ozoli.db

import concurrent.ExecutionContext.Implicits.global

import java.net.URL
import java.time.{ZoneOffset, LocalDateTime}
import java.time.format.DateTimeFormatter

import com.typesafe.config.ConfigFactory
import io.ozoli.blog.domain.BlogEntry
import io.ozoli.blog.util.RssReader
import org.bson.BsonDateTime

import org.mongodb.scala.bson.BsonTransformer
import org.mongodb.scala.{MongoCollection, MongoDatabase, MongoClient, Document}

/**
 * Here the current Blog Entries are read from the Feed URL
 * and any previous Blog Entries in the database are replaced with the new entries.
 */
trait DatabaseConfiguration {
  val conf = ConfigFactory.load("application.conf")

  protected var mongoClient: MongoClient = MongoClient()

  def getMongoClient : MongoClient = mongoClient

  var database: MongoDatabase = getMongoClient.getDatabase("ozoliblogdb")

  var blogCollection: MongoCollection[Document] = database.getCollection("blogs")

  // Read the current blog entries from the Feed URL
  lazy val currentBlogEntries : Seq[BlogEntry] = {
    RssReader.extractRss(new URL(conf.getString("blog.rss.uri")))
  }

  implicit object TransformLocalDateTime extends BsonTransformer[LocalDateTime] {
    def apply(dateTime: LocalDateTime): BsonDateTime = {
      new BsonDateTime(dateTime.toInstant(ZoneOffset.UTC).toEpochMilli)
    }
  }

  // Insert all the BlogEntries read from RSS
  val documents = currentBlogEntries map { blogEntry: BlogEntry => Document(
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
      println(s"total # of documents after inserting BlogEntries from RSS: $countResult")
  }
}
