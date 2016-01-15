package io.ozoli.blog

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

import com.github.simplyscala.{MongoEmbedDatabase, MongodProps}
import com.mongodb.client.model.Filters
import io.ozoli.blog.domain.BlogEntry
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.time.{Millis, Seconds, Span}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import org.mongodb.scala
import org.mongodb.scala._
import org.scalatest.{BeforeAndAfterAll, FunSuite}

/**
 * Integration test for the MongoDB persistence.
 */
class BlogServiceDbTest extends FunSuite with MongoEmbedDatabase with BeforeAndAfterAll with ScalaFutures {

  // default timeout for eventually trait to slow the tests a little when ran with sbt
  implicit override val patienceConfig =
    PatienceConfig(timeout = Span(5, Seconds), interval = Span(1500, Millis))

  var mongoProps: MongodProps = null
  var blogCollection: scala.MongoCollection[Document] = null
  var mongoClient: MongoClient = null

  var blogEntryOne : BlogEntry = BlogEntry(LocalDateTime.now(), "A One Blog Entry", "link-title",
    "The text int the body that could go on and on...", "cat1 cat2")

  var blogEntryTwo : BlogEntry = BlogEntry(LocalDateTime.now(), "A Two Blog Entry", "link-title",
    "The text int the body that could go on and on...", "2cat1 cat2")

  val documents: List[Document] = List(Document(
    "_id" -> 1,
    "pubDate" -> DateTimeFormatter.ISO_DATE_TIME.format(blogEntryOne.pubDate),
    "title" -> blogEntryOne.title,
    "linkTitle" -> blogEntryOne.linkTitle,
    "body" -> blogEntryOne.body,
    "category" -> blogEntryOne.category),
    Document(
      "_id" -> 2,
      "pubDate" -> DateTimeFormatter.ISO_DATE_TIME.format(blogEntryTwo.pubDate),
      "title" -> blogEntryTwo.title,
      "linkTitle" -> blogEntryTwo.linkTitle,
      "body" -> blogEntryTwo.body,
      "category" -> blogEntryTwo.category)
  )

  override def beforeAll() {
    mongoProps = mongoStart(port = 27017)
    mongoClient = MongoClient()
    val db = mongoClient.getDatabase("ozoliblogdb")
    println("creating blogs collection")
    val dropCollectionFuture = db.getCollection("blogs").drop().toFuture()
    val createCollectionFutuere = db.createCollection("blogs").toFuture()
    val insertDocumnetFuture = db.getCollection("blogs").insertMany(documents).toFuture()
    val countDocsFuture = db.getCollection("blogs").count().toFuture()
    val f = Future.sequence(Seq(dropCollectionFuture, createCollectionFutuere, insertDocumnetFuture, countDocsFuture))
    f.onComplete { _ => blogCollection = db.getCollection("blogs") }
  }

  test("test find blog one") {
    println("test find blog one")
    val db = mongoClient.getDatabase("ozoliblogdb")
    val blog = db.getCollection("blogs").find(Filters.eq("title", blogEntryOne.title)).first().toFuture()
    whenReady(blog) { blogResult => assert(blogResult.head == documents.seq.head) }
  }

  test("test find blog two") {
    println("test find blog two")
    val db = mongoClient.getDatabase("ozoliblogdb")
    val blog = db.getCollection("blogs").find(Filters.eq("title", blogEntryTwo.title)).first().toFuture()
    whenReady(blog) { blogResult => assert(blogResult.head == documents(1)) }
  }

  test("test find all blogs") {
    val db = mongoClient.getDatabase("ozoliblogdb")
    val blogs = db.getCollection("blogs").find().toFuture()
    whenReady(blogs) { blogResult => assert(blogResult.seq.size == documents.size) }
  }

  test("test find non existent blog") {
    val db = mongoClient.getDatabase("ozoliblogdb")
    val blog = db.getCollection("blogs").find(Filters.eq("title", "An Unknown Blog Entry")).first().toFuture()
    whenReady(blog) { blogResult => assert(blogResult == List.empty) }
  }

  override def afterAll() {
    mongoClient.close()
    mongoStop(mongoProps)
    println("Finished DBTest")
  }
}

