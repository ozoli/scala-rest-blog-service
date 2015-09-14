package io.ozoli.blog

import java.net.URL

import org.joda.time.format.{DateTimeFormat, DateTimeFormatter}

import scala.xml.{Elem, Node, XML}

/**
 * A RSS Reader from XML or URL create Blog Entry's
 */
object RssReader {

  lazy val pubDateFormat : DateTimeFormatter = DateTimeFormat.forPattern("EEE, dd MMM yyyy HH:mm:ss Z")

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
  def extractRss(url : URL) : Seq[BlogEntry] = extractBlogEntries(XML.load(url.openConnection.getInputStream))

  /**
   * Given a channel node from an RSS feed, returns all of the BlogEntries
   * @param channel the XML Node to read all the blog entries from
   * @return a Seq of BlogEntry
   */
  private def extractBlogEntries(channel : Node) : Seq[BlogEntry] = {
    for (item <- channel \\ "item") yield {
      BlogEntry(1, pubDateFormat.parseDateTime((item \ "pubDate").text).toLocalDateTime,
        (item \ "title").text,
        (item \ "description").text,
        (channel \\ "category").map(_.text).mkString(" "))
    }
  }

}
