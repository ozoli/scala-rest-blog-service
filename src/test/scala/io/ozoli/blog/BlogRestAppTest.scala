package io.ozoli.blog

import java.time.LocalDateTime

import akka.actor.ActorSystem
import akka.http.model.HttpMethods.{OPTIONS, POST}
import akka.http.model.headers.RawHeader
import akka.stream.scaladsl.{Sink, Source}
import akka.testkit.TestKit
import com.github.simplyscala.{MongodProps, MongoEmbedDatabase}
import com.mongodb.client.model.Filters
import de.flapdoodle.embed.mongo.Command
import de.flapdoodle.embed.mongo.config.RuntimeConfigBuilder
import de.flapdoodle.embed.process.config.io.ProcessOutput
import io.ozoli.blog.domain.BlogEntry
import io.ozoli.db.DB
import org.mongodb.scala.MongoClient
import org.scalatest._
import org.scalatest.concurrent._
import akka.http.Http
import akka.http.model._
import akka.stream.ActorFlowMaterializer
import akka.http.unmarshalling._
import org.scalatest.time.{Millis, Seconds, Span}

import scala.concurrent.ExecutionContext

/**
 * Integration test for the BlogRestApp
 */
class BlogRestAppTest extends FlatSpec with DB with MongoEmbedDatabase
  with Matchers with ScalaFutures with BeforeAndAfterAll {

  override implicit lazy val system = ActorSystem("test-system")
  override implicit def dispatcher: ExecutionContext = system.dispatcher

  implicit override val patienceConfig =
    PatienceConfig(timeout = Span(5, Seconds), interval = Span(2500, Millis))

  implicit val fm = ActorFlowMaterializer()

  var mongoProps: MongodProps = null
  var started = false

  val server = new BlogRestApp()

  override def beforeAll() = {
    println("Starting server")
    server.start()
  }

  override def afterAll() = {
    println("Stopping server")
    server.stop()
    mongoStop(mongoProps)
    TestKit.shutdownActorSystem(system)
    println("Finished AppSpec")
  }

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

  def sendRequest(req: HttpRequest) =
    Source.single(req).via(
      Http().outgoingConnection(host = "localhost", port = 9999).flow
    ).runWith(Sink.head)

  "find blog with exact title" should "find blog with title 'So this has grown on me a bit...'" in {
    val blog = blogCollection.find(Filters.eq("title", "So this has grown on me a bit...")).first().toFuture()
    whenReady(blog) { blogResult => assert(blogResult.head.get("body") == documents(4).get("body")) }
  }

  "getAllBlogEntries from DB trait" should "return the same amount of documents" in  {
    val blogs = getAllBlogEntries
    whenReady(blogs) { blogResult => assert(blogResult.seq.size == documents.size) }
  }

  "find non existent blog" should "not be found" in {
    val blog = database.getCollection("blogs").find(Filters.eq("title", "An Unknown Blog Entry")).first().toFuture()
    whenReady(blog) { blogResult => assert(blogResult == List.empty) }
  }

  "findBlogByLinkTitle from DB trait" should "find a blog of title 'fourth-of-july'" in {
    val blog = findBlogByLinkTitle("fourth-of-july")
    whenReady(blog) {
      blogResult => {
        assert(blogResult.head.body == documents(3).get("body").get.asString().getValue)
      }
    }
  }

  "BlogServer" should "return Blogs as JSON on a GET to /blog/fourth-of-july" in {
    val request = sendRequest(HttpRequest(uri = "/blog/fourth-of-july"))
    whenReady(request) { response =>
      val stringFuture = Unmarshal(response.entity).to[String]
      whenReady(stringFuture) { str =>
        str should include("Anyway, I liked both LivePlasma and MusicMap, " +
          "the only difference I really noted in my search is MusicMap ")
      }
    }
  }

  "addBlogEntry from DB trait" should "increase number of blogs" in {
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

  "PING on BlogServer" should "return PONG! on a GET to /ping" in {
    val request = sendRequest(HttpRequest(uri = "/ping"))
    whenReady(request) { response =>
      val stringFuture = Unmarshal(response.entity).to[String]
      whenReady(stringFuture) { str =>
        str should include("PONG!")
      }
    }
  }

  "BlogService" should "return 404 on a GET to /foo" in {
    val request = sendRequest(HttpRequest(uri = "/foo"))
    whenReady(request) { response =>
      response.status shouldBe StatusCodes.NotFound
    }
  }

  "BlogServer" should "return Blogs as JSON on a GET to /blogs" in {
    val request = sendRequest(HttpRequest(uri = "/blogs"))
    whenReady(request) { response =>
      val stringFuture = Unmarshal(response.entity).to[String]
      whenReady(stringFuture) { str =>
        str should include("So this has grown on me a bit...")
      }
    }
  }

  "BlogServer" should "return Access-Control-Allow-Origin on a OPTIONS to /blogs" in {
    val request = sendRequest(HttpRequest(OPTIONS, uri = "/blogs"))
    whenReady(request) { response =>
      response.getHeader("Access-Control-Allow-Origin").get.value() shouldBe "*"
    }
  }

  "BlogServer" should "return 200 OK on a valid POST to /blogs" in {
    val postHeaders = List(RawHeader("Connection", "close"),
      RawHeader("Link", "<http://olivercarr.blogspot.com/feeds/posts/default>;" +
        "rel=self,<http://pubsubhubbub.appspot.com/>;rel=hub"),
      RawHeader("Via", "1.1 vegur"),
      RawHeader("Content-Length", "2118"),
      RawHeader("X-Request-Id", "e341227d-f86a-4e2f-a30b-5cb3f90cc93f"),
      RawHeader("Host", "requestb.in"),
      RawHeader("Connect-Time", "1"),
      RawHeader("From", "googlebot(at)googlebot.com"),
      RawHeader("User-Agent", "FeedFetcher-Google; (+http://www.google.com/feedfetcher.html)"),
      RawHeader("Content-Type", "application/rss+xml"),
      RawHeader("Accept", "*/*"),
      RawHeader("Pragma", "no-cache"),
      RawHeader("Total-Route-Time", "0"),
      RawHeader("Cache-Control", "no-cache,max-age=0"),
      RawHeader("Accept-Encoding", "gzip,deflate"))

    val data = "<?xml version='1.0' encoding='UTF-8'?>\n<rss xmlns:atom=\"http://www.w3.org/2005/Atom\" xmlns:" +
      "openSearch=\"http://a9.com/-/spec/opensearchrss/1.0/\" xmlns:blogger=\"http://schemas.google.com/blogger/2008\"" +
      " xmlns:georss=\"http://www.georss.org/georss\" xmlns:gd=\"http://schemas.google.com/g/2005\" " +
      "xmlns:thr=\"http://purl.org/syndication/thread/1.0\" xmlns:feedburner=\"http://rssnamespace.org/feedburner" +
      "/ext/1.0\" version=\"2.0\"><channel><atom:id>tag:blogger.com,1999:blog-2293822506107569095</atom:id>" +
      "<lastBuildDate>Thu, 03 Dec 2015 21:28:43 +0000</lastBuildDate><category>events amsterdam gig tickets" +
      "</category><category>google chrome mac web browser development beta</category><title>Oliver Carr</title>" +
      "<description>Random posts about various topics ranging from software development, travel, food and photography." +
      " </description><link>http://olivercarr.blogspot.com/</link><managingEditor>noreply@blogger.com (Oliver Carr)" +
      "</managingEditor><generator>Blogger</generator><openSearch:totalResults>6</openSearch:totalResults>" +
      "<openSearch:startIndex>1</openSearch:startIndex><openSearch:itemsPerPage>25</openSearch:itemsPerPage>" +
      "<atom10:link xmlns:atom10=\"http://www.w3.org/2005/Atom\" rel=\"self\" type=\"application/rss+xml\" " +
      "href=\"http://feeds.feedburner.com/OliverCarrsBlog\" /><feedburner:info uri=\"olivercarrsblog\" />" +
      "<atom10:link xmlns:atom10=\"http://www.w3.org/2005/Atom\" rel=\"hub\" href=\"http://pubsubhubbub.appspot.com/\"" +
      " /><item><guid isPermaLink=\"false\">tag:blogger.com,1999:blog-2293822506107569095.post-1787358490959815244" +
      "</guid><pubDate>Thu, 03 Dec 2015 21:28:00 +0000</pubDate><atom:updated>2015-12-03T22:28:43.975+01:00" +
      "</atom:updated><title>Scala </title><description>If you are a developer then you must try Scala. What a " +
      "language!&lt;img src=\"http://feeds.feedburner.com/~r/OliverCarrsBlog/~4/8XnkBsW8kzY\" height=\"1\" width=\"1\"" +
      " alt=\"\"/&gt;</description><link>http://feedproxy.google.com/~r/OliverCarrsBlog/~3/8XnkBsW8kzY/scala.html" +
      "</link><author>noreply@blogger.com (Oliver Carr)</author><thr:total>0</thr:total><feedburner:origLink>" +
      "http://olivercarr.blogspot.com/2015/12/scala.html</feedburner:origLink></item></channel></rss>"

    var previousBlogsSize = 0
    var newBlogsSize = 0
    var statusCode = StatusCode.int2StatusCode(500)

    val addNewBlog = for {
      previousBlogs <- getAllBlogEntries
      request <- sendRequest(HttpRequest(POST, uri = "/blogs", headers = postHeaders, entity = data))
      newBlogs <- getAllBlogEntries
    } yield {
        previousBlogsSize = previousBlogs.size
        newBlogsSize = newBlogs.size
        statusCode = request.status
    }
    whenReady(addNewBlog) { res => assert(newBlogsSize == previousBlogsSize + 1 && statusCode == StatusCodes.OK) }
  }

}
