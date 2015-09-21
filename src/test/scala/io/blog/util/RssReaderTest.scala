package io.blog.util

import io.ozoli.blog.domain.BlogEntry
import io.ozoli.blog.util.RssReader
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.time.{Seconds, Span}
import org.scalatest.{BeforeAndAfter, FunSuite}

import scala.xml.XML

/**
 * Unit test for RssReader
 */
class RssReaderTest extends FunSuite with BeforeAndAfter with ScalaFutures {
  implicit override val patienceConfig = PatienceConfig(timeout = Span(5, Seconds))

  test("Create from RSS Blog Entry from File") {
    val blogEntryXml = XML.load(getClass.getResource("/blog-entry.xml").openStream())
    val blogEntries : Seq[BlogEntry] = RssReader.getBlogEntries(blogEntryXml)
    assert(blogEntries.size == 1)
    assert(blogEntries.head.title == "Creme Brulee or Creme Catalan?")
    assert(blogEntries.head.category ==
      "events amsterdam gig tickets google chrome mac web browser development beta")
  }

  test("Create from RSS Blog Entry from File PDT timezone") {
    val blogEntryXml = XML.load(getClass.getResource("/blog-entry-PDT-timezone.xml").openStream())
    val blogEntries : Seq[BlogEntry] = RssReader.getBlogEntries(blogEntryXml)
    assert(blogEntries.size == 1)
    assert(blogEntries.head.title == "Creme Brulee or Creme Catalan?")
    assert(blogEntries.head.category ==
      "events amsterdam gig tickets google chrome mac web browser development beta")
  }

}