package io.ozoli.blog

import java.net.URL

import org.joda.time.DateTime

import scala.xml.{Elem, Node, XML}

object RssReader {

  def getBlogEntries(xml: Elem) : Seq[BlogEntry] = {
    val channel = xml \\ "channel"
    extractBlogEntries(channel.head)
  }

  def extractRss(urlStr : String) : Seq[BlogEntry] = {
    // Given a URL, returns a Seq of RSS data in the form:
    // ("channel", ["article 1", "article 2"])
    val url = new URL(urlStr)
    val conn = url.openConnection
    val xml = XML.load(conn.getInputStream)

    extractBlogEntries(xml)
  }

  private def extractBlogEntries(channel : Node) : Seq[BlogEntry] = {
    // Given a channel node from an RSS feed, returns all of the BlogEntries
    for (item <- channel \\ "item") yield {
      BlogEntry(1, new DateTime(),
        (item \ "title").text,
        (item \ "description").text,
        (channel \\ "category").map(_.text).mkString(" "))
    }
  }

}