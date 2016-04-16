package io.ozoli.db

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

import akka.actor.ActorSystem
import com.mongodb.client.model.Filters
import io.ozoli.blog.domain.BlogEntry
import org.mongodb.scala.Completed
import org.mongodb.scala.bson.Document

import scala.concurrent._

/**
 * Blog Database operations using the configuration backed by MongoDB.
 */
trait DB extends DatabaseConfiguration {
  def system : ActorSystem
  implicit def dispatcher : ExecutionContext

  /**
   * Get all the BlogEntries from the database.
   * @return the Future to get all the Blog Entries.
   */
  def getAllBlogEntries : Future[Seq[BlogEntry]] =
    for {
      blogData <- database.getCollection("blogs").find().toFuture()
    } yield {
      blogData.seq.map(document => toBlogEntry(document))
    }

  /**
   * Add the given BlogEntry to the database.
   * @param blog the BlogEntry to add
   * @return the Future to add the BlogEntry
   */
  def addBlogEntry(blog: BlogEntry) : Future[Completed] =
    database.getCollection("blogs").insertOne(Document(
      "pubDate" -> DateTimeFormatter.ISO_DATE_TIME.format(blog.pubDate),
      "title" -> blog.title,
      "linkTitle" -> blog.linkTitle,
      "body" -> blog.body,
      "category" -> blog.category)).head()

  /**
   * Find a Blog Entry by link title from the database
   * @param linkTitle the blog ID to use
   * @return the Future to find the blog entry by id in the database
   */
  def findBlogByLinkTitle(linkTitle: String) : Future[Seq[BlogEntry]] =
    for {
      blog <- database.getCollection("blogs").find(Filters.eq("linkTitle", linkTitle)).first().toFuture()
    } yield {
      blog.seq.map(document => toBlogEntry(document))
    }

  /**
   * Convert a Document to a BlogEntry
   * @param document the Document to convert.
   * @return the corresponding BlogEntry for the given Document
   */
  private def toBlogEntry(document: Document): BlogEntry =
    BlogEntry(
      Integer.valueOf(document.get("id").get.asString().getValue),
      LocalDateTime.parse(document.get("pubDate").get.asString().getValue, DateTimeFormatter.ISO_DATE_TIME),
      document.get("title").get.asString().getValue,
      document.get("linkTitle").get.asString().getValue,
      document.get("body").get.asString().getValue,
      document.get("category").get.asString().getValue)
}
