package io.ozoli.blog.util

import java.net.URL
import java.time.LocalDateTime
import java.time.format.{DateTimeFormatterBuilder, DateTimeFormatter}

import grizzled.slf4j.Logger
import io.ozoli.blog.domain.BlogEntry

import scala.xml.{Elem, Node, XML}

/**
 * A RSS Reader from XML or URL create Blog Entry's
 */
object RssReader {
  val logger = Logger[this.type]

  // support different formats of the publish date in the Atom RSS Feed.
   lazy val pubDateFormat : DateTimeFormatter = new DateTimeFormatterBuilder().appendPattern(
    "[EEE, dd MMM yyyy HH:mm:ss Z][EEE, dd MMM yyyy HH:mm:ss z]").toFormatter

  /**
   * For the given XML retreive the BlogEntry's found
   * @param xml the XML element to parse
   * @return the Seq of BlogEntry's found
   */
  def getBlogEntries(xml: Elem) : Seq[BlogEntry] = extractBlogEntries((xml \\ "channel").head)

  /**
   * For the given URL extract the BlogEntry's contained within
   * @param url the URL to use
   * @return the Seq of BlogEntry's found
   */
  def extractRss(url : URL) : Seq[BlogEntry] = {
      try {
        extractBlogEntries(XML.load(url.openConnection.getInputStream))
      }
      catch {
        case e : Exception =>
          logger.error(s"Error Finding Blogs for URL $url ${e.getMessage} $e \nFallback Blog XML in use!")
          getBlogEntries(XML.load(getClass.getResource("/blog-entries.xml").openStream()))
      }
  }

  /**
   * Given a channel node from an RSS feed, returns all of the BlogEntries
   * to create the link title remove all non characters and then replace spaces with -'s
   * @param channel the XML Node to read all the blog entries from
   * @return a Seq of BlogEntry
   */
  private def extractBlogEntries(channel : Node) : Seq[BlogEntry] = {
    for (item <- channel \\ "item") yield {
      BlogEntry(LocalDateTime.parse((item \ "pubDate").text, pubDateFormat),
        (item \ "title").text,
        (item \ "title").text.replaceAll("[^a-zA-Z|^\\s]", "").replaceAll(" ", "-").toLowerCase,
        (item \ "description").text,
        (channel \\ "category").map(_.text).mkString(" "))
    }
  }

}
