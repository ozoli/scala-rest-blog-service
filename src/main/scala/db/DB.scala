package db

import akka.actor.ActorSystem
import com.github.mauricio.async.db.{RowData, QueryResult}
import io.ozoli.blog.domain.BlogEntry
import org.joda.time.LocalDateTime
import org.joda.time.format.{DateTimeFormatter, DateTimeFormat}

import scala.concurrent._

trait DB {
  def system : ActorSystem
  implicit def dispatcher : ExecutionContext

  lazy val pool = new Pool(system).pool

  lazy val dateTimePattern: DateTimeFormatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss")

  /**
   * Creates a prepared statement with the given query
   * and passes it to the connection pool with given values.
   * @return Seq[RowData] of the query
   */
  def fetch(query: String, values: Any*): Future[Option[Seq[RowData]]] = execute(query, values: _*).map(_.rows)

  /**
   * Creates a prepared statement with the given query
   * and passes it to the connection pool with given values.
   * @param query the SQL query to execute
   * @param values the values for the prepared statement if needed
   * @return the Future of the QueryResult for the given query
   */
  def execute(query: String, values: Any*): Future[QueryResult] =
    values match {
      case Nil => pool.sendQuery(query)
      case list => pool.sendPreparedStatement(query, values)
    }

  /**
   * Get all the BlogEntries from the database.
   * @return the Future to get all the Blog Entries.
   */
  def getAllBlogEntries : Future[Seq[BlogEntry]] =
    fetch("SELECT * FROM BLOGENTRIES").map(queryResult => for { rowData <- queryResult.get } yield getData(rowData))

  /**
   * Add the given BlogEntry to the database.
   * @param blog the BlogEntry to add
   * @return the Future to add the BlogEntry
   */
  def addBlogEntry(blog: BlogEntry) : Future[QueryResult] =
    execute("INSERT INTO BLOGENTRIES (PUB_DATE, TITLE, BODY, CATEGORY) VALUES (\"%s\", \"%s\", \"%s\", \"%s\")"
      .format(dateTimePattern.print(blog.pubDate), blog.title.replace("\"","\\\""),
        blog.body.replace("\"","\\\""), blog.category))

  /**
   * Find a Blog Entry by ID from the database
   * @param blogId the blog ID to use
   * @return the Future to find the blog entry by id in the database
   */
  def findBlogById(blogId: Long) : Future[Option[Seq[RowData]]] =
    fetch("SELECT * FROM BLOGENTRIES WHERE id=%d".format(blogId))

  /**
   * Convert the given Row to JSON
   * @param rowData the given BlogEntry row to convert to JSON
   * @return the BlogEntry from the row data.
   */
  def getData(rowData: RowData) : BlogEntry = {
    BlogEntry(rowData("id").asInstanceOf[Long], rowData("PUB_DATE").asInstanceOf[LocalDateTime],
      rowData("TITLE").asInstanceOf[String], rowData("BODY").asInstanceOf[String],
      rowData("CATEGORY").asInstanceOf[String])
  }

}
