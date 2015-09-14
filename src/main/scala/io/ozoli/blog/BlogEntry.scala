package io.ozoli.blog

import slick.driver.MySQLDriver.api._
import com.github.tototoshi.slick.MySQLJodaSupport._

import org.joda.time.LocalDateTime


/**
 * A representation of an article in a RSS Feed here call a BlogEntry
 *
 * @param id the ID in the database
 * @param pubDate the publication date
 * @param title the title
 * @param body the body
 * @param category the category string a space separated string of tags
 */
case class BlogEntry(id: Long, pubDate: LocalDateTime, title: String, body: String, category: String)

// A BlogEntries table with 5 columns: id, pubDate, title, body, category
class BlogEntries(tag: Tag) extends Table[BlogEntry](tag, "BLOGENTRIES") {

  def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
  def pubDate = column[LocalDateTime]("PUB_DATE")
  def title = column[String]("TITLE")
  def body = column[String]("BODY")
  def category = column[String]("CATEGORY")

  def * = (id, pubDate, title, body, category) <> (BlogEntry.tupled, BlogEntry.unapply _)
}


