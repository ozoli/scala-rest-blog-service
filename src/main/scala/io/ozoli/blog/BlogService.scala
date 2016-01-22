package io.ozoli.blog

import akka.actor._
import grizzled.slf4j.Logger

import io.ozoli.blog.domain.BlogEntryJsonProtocol._
import io.ozoli.blog.util.{CORSSupport, RssReader}
import io.ozoli.db.DB

import scala.concurrent._
import scala.util.{Failure, Success}
import scala.xml.XML

import spray.can.Http
import spray.http._
import spray.http.CacheDirectives.`max-age`
import spray.http.HttpHeaders.`Cache-Control`
import spray.http.StatusCodes.{InternalServerError, NotFound, OK}
import spray.httpx.SprayJsonSupport
import spray.json._
import HttpMethods._

/**
 * A RESTful web service for Blog Entries
 */
class BlogService extends Actor with ActorLogging with DB with SprayJsonSupport with CORSSupport {

  private lazy val CacheHeader = (maxAge: Long) => `Cache-Control`(`max-age`(maxAge)) :: Nil
  private lazy val MaxAge = 2592000.toLong  // 30 days (60 sec * 60 min * 24 hours * 30 days)
  private lazy val MaxAge404 = 600l

  private lazy val blogPerTitleUri = """(/blog/)([\w\.|\-]+)"""

  val logger = Logger[this.type]

  def system = context.system

  implicit def dispatcher: ExecutionContext = context.dispatcher

  override def receive = {
    // when a new connection comes in we register ourselves as the connection handler
    case _: Http.Connected => sender ! Http.Register(self)

    case HttpRequest(OPTIONS, Uri.Path("/blogs"), headers, _, _) =>
      logger.info(s"OPTIONS /blogs Requested $headers")
      sender ! HttpResponse(status = OK).withHeaders(CORSHeadersOptionsBlogs)

    case HttpRequest(GET, Uri.Path("/blogs"), headers, _, _) =>
      val client = sender()
      getAllBlogEntries.onComplete {
        case Success(result) =>
          client ! HttpResponse(status = OK).withEntity(
            HttpEntity(ContentTypes.`application/json`, result.toJson.compactPrint))
                       .withHeaders(CORSHeaders)
        case Failure(ex) =>
          logger.error(s"Error Finding Blogs ${ex.getMessage} $ex")
          client ! HttpResponse(status = InternalServerError, entity = "Error Finding Blogs")
                               .withHeaders(CacheHeader(MaxAge404)).withHeaders(CORSHeaders)
      }

    case HttpRequest(GET, Uri.Path(path), _, _, _) if path matches blogPerTitleUri =>
      val client = sender()
      findBlogByLinkTitle(path.split("/blog/").toList.last).onComplete {
        case Success(blog) =>
          client ! HttpResponse(status = OK).withEntity(
            HttpEntity(ContentTypes.`application/json`, blog.head.toJson.compactPrint))
            .withHeaders(CacheHeader(MaxAge)).withHeaders(CORSHeaders)
        case Failure(ex) =>
          logger.error(s"Error Finding Blog Link Title ${ex.getMessage} $ex")
          client ! HttpResponse(status = InternalServerError, entity = "Error Finding Blog Link Title")
            .withHeaders(CacheHeader(MaxAge404)).withHeaders(CORSHeaders)
      }

    case HttpRequest(POST, Uri.Path("/blogs"), _, body, _) =>
      val client = sender()
      addBlogEntry(RssReader.getBlogEntries(XML.loadString(body.asString)).head).onComplete {
      case Success(result) =>
        client ! HttpResponse(status = OK).withHeaders(CORSHeaders)
      case Failure(ex) =>
        logger.error(s"Error Adding Blog ${ex.getMessage} $ex New Blog $body")
        client ! HttpResponse(status = InternalServerError, entity = "Error Adding Blog")
          .withHeaders(CacheHeader(MaxAge404)).withHeaders(CORSHeaders)
      }

    case HttpRequest(GET, Uri.Path("/ping"), _, _, _) =>
      sender ! HttpResponse(status = OK, entity = "PONG!").withHeaders(CORSHeaders)

    case unhandledRequest : HttpRequest =>
      logger.error(s"Unknown Resource Requested ${unhandledRequest.method} ${unhandledRequest.uri}")
      sender ! HttpResponse(status = NotFound, entity = "Unknown resource!")
                            .withHeaders(CacheHeader(MaxAge404)).withHeaders(CORSHeaders)
  }

}
