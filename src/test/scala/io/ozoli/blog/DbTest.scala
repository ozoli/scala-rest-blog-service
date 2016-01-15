package io.ozoli.blog

import java.time.LocalDateTime

import akka.actor.ActorSystem
import akka.testkit.{TestKit, TestKitBase}
import com.github.simplyscala.{MongoEmbedDatabase, MongodProps}
import com.mongodb.client.model.Filters
import de.flapdoodle.embed.mongo.Command
import de.flapdoodle.embed.mongo.config.RuntimeConfigBuilder
import de.flapdoodle.embed.process.config.io.ProcessOutput
import io.ozoli.blog.domain.BlogEntry
import io.ozoli.db.DB
import org.mongodb.scala._
import org.scalatest._
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.time.{Seconds, Span, Millis}

import _root_.scala.concurrent.ExecutionContext

/**
 * Integration test for the MongoDB persistence.
 * Uses http://feeds.feedburner.com/YourBlog?fmt=xml for blog source
 */
class DbTest extends TestKitBase
  with DB with MongoEmbedDatabase with FunSuiteLike with BeforeAndAfterAll with ScalaFutures {

  override implicit lazy val system = ActorSystem()
  override implicit def dispatcher: ExecutionContext = system.dispatcher

  // default timeout for eventually trait to slow the tests a little when ran with sbt
  implicit override val patienceConfig =
    PatienceConfig(timeout = Span(5, Seconds), interval = Span(1500, Millis))

  var mongoProps: MongodProps = null
  var started = false

  override def getMongoClient : MongoClient = {
    if (!started) {
      mongoProps = mongoStart(port = 27017, runtimeConfig = new RuntimeConfigBuilder()
        .defaults(Command.MongoD)
        .processOutput(ProcessOutput.getDefaultInstanceSilent)
        .build())
      started = true
    }
    mongoClient
  }

  // TODO fix code and or embedded Mongo DB setup so this fake test is not needed
  test("test find count") {
    // makes sure the database is setup before the test runs
    whenReady(blogCollection.count().toFuture()) { result => assert(result.head >= 0) }
  }

  test("test find blog with regex") {
    val blog = blogCollection.find(Filters.regex("title", ".*this has grown*")).first().toFuture()
    whenReady(blog) { blogResult => assert(blogResult.head.get("body") == documents(4).get("body")) }
  }

  test("test find blog with exact title") {
    val blog = blogCollection.find(Filters.eq("title", "So this has grown on me a bit...")).first().toFuture()
    whenReady(blog) { blogResult => assert(blogResult.head.get("body") == documents(4).get("body")) }
  }

  test("test getAllBlogEntries from DB trait") {
    val blogs = getAllBlogEntries
    whenReady(blogs) { blogResult => assert(blogResult.seq.size == documents.size) }
  }

  test("test find non existent blog") {
    val blog = database.getCollection("blogs").find(Filters.eq("title", "An Unknown Blog Entry")).first().toFuture()
    whenReady(blog) { blogResult => assert(blogResult == List.empty) }
  }

  test("test findBlogByLinkTitle from DB trait") {
    val blog = findBlogByLinkTitle("fourth-of-july")
    whenReady(blog) {
      blogResult => {
        assert(blogResult.head.body == documents(3).get("body").get.asString().getValue)
      }
    }
  }

  test("test addBlogEntry from DB trait") {
    val blogEntryOne : BlogEntry = BlogEntry(LocalDateTime.now(), "A One Blog Entry", "link-title",
      "The text int the body that could go on and on...", "cat1 cat2")

    var previousBlogsSize = 0
    var newBlogsSize = 0

    val prevAddnew = for {
      previousBlogs <- getAllBlogEntries
      addBlog <- addBlogEntry(blogEntryOne)
      newBlogs <- getAllBlogEntries
    } yield {
      previousBlogsSize = previousBlogs.size
      newBlogsSize = newBlogs.size
    }
    whenReady(prevAddnew) { res => assert(newBlogsSize == previousBlogsSize + 1) }
  }

  override def afterAll() {
    mongoStop(mongoProps)
    TestKit.shutdownActorSystem(system)
    println("Finished DBTest")
  }
}
