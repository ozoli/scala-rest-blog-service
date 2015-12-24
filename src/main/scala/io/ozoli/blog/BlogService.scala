package io.ozoli.blog

import grizzled.slf4j.Logger
import io.ozoli.blog.util.{CORSSupport, RssReader}
import akka.actor._

import io.ozoli.db.DB

import scala.concurrent._
import scala.util.{Failure, Success}
import scala.xml.XML

import spray.can.Http
import spray.http._
import spray.http.CacheDirectives.`max-age`
import spray.http.HttpHeaders.`Cache-Control`
import spray.httpx.SprayJsonSupport
import spray.json._
import HttpMethods._

import io.ozoli.blog.domain.BlogEntryJsonProtocol._

/**
 * A RESTful web service for Blog Entries
 */
class BlogService extends Actor with ActorLogging with DB with SprayJsonSupport with CORSSupport {

  private lazy val CacheHeader = (maxAge: Long) => `Cache-Control`(`max-age`(maxAge)) :: Nil
  private lazy val MaxAge = 2592000.toLong  // 30 days (60 sec * 60 min * 24 hours * 30 days)
  private lazy val MaxAge404 = 600l

  private lazy val blogPerIdUri = "(/blog/)\\d+"
  private lazy val blogIdRegEx = "\\d+".r

  val logger = Logger[this.type]

  override def system = context.system

  override implicit def dispatcher: ExecutionContext = context.dispatcher

  override def receive = {
    // when a new connection comes in we register ourselves as the connection handler
    case _: Http.Connected => sender ! Http.Register(self)

    case HttpRequest(OPTIONS, Uri.Path("/blogs"), headers, _, _) =>
      logger.info(s"OPTIONS /blogs Requested $headers")
      sender ! HttpResponse(status = 200).withHeaders(CORSHeadersOptionsBlogs)

    case HttpRequest(GET, Uri.Path("/blogs"), headers, _, _) =>
      val client = sender()
      getAllBlogEntries.onComplete {
        case Success(result) =>
          client ! HttpResponse(status = 200).withEntity(
            HttpEntity(ContentTypes.`application/json`, result.toJson.compactPrint))
                       .withHeaders(CORSHeaders)
        case Failure(ex) =>
          logger.error(s"Error Finding Blogs ${ex.getMessage} $ex")
          client ! HttpResponse(status = 500, entity = "Error Finding Blogs")
                               .withHeaders(CacheHeader(MaxAge404)).withHeaders(CORSHeaders)
      }

    case HttpRequest(GET, Uri.Path(path), _, _, _) if path matches blogPerIdUri =>
      val client = sender()
      blogIdRegEx.findFirstIn(path) match {
        case Some(blogId) =>
          findBlogById(blogId.toLong).onComplete {
            case Success(Some(resultSet)) if resultSet.nonEmpty =>
              client ! HttpResponse(status = 200).withEntity(
                HttpEntity(ContentTypes.`application/json`, getData(resultSet.head).toJson.compactPrint))
                .withHeaders(CacheHeader(MaxAge)).withHeaders(CORSHeaders)
            case Success(Some(resultSet)) if resultSet.isEmpty =>
              logger.error(s"Error Finding Blog ID $blogId")
              client ! HttpResponse(status = 400,
                entity = "Blog ID Not Found").withHeaders(CacheHeader(MaxAge404)).withHeaders(CORSHeaders)
            case Success(Some(_)) =>
              logger.error(s"Unknown Error Finding Blog ID $blogId")
              client ! HttpResponse(status = 400,
                entity = "Unknown Error Finding Blog ID").withHeaders(CacheHeader(MaxAge404)).withHeaders(CORSHeaders)
            case Failure(ex) =>
              logger.error(s"Error Finding Blog ID ${ex.getMessage} $ex}")
              client ! HttpResponse(status = 500, entity = "Error Finding Blog ID")
                                    .withHeaders(CacheHeader(MaxAge404)).withHeaders(CORSHeaders)
          }
        case None => logger.error(s"Error Finding Blog ID for URI $path")
      }

    case HttpRequest(POST, Uri.Path("/blogs"), _, body, _) =>
      val client = sender()
      addBlogEntry(RssReader.getBlogEntries(XML.loadString(body.asString)).head).map(
        queryResult => queryResult.rowsAffected match {
          case 1 => client ! HttpResponse(entity = "OK").withHeaders(CORSHeaders)
          case 0 =>
            logger.error(s"Zero Rows Affected Inserting new Blog $body")
            client ! HttpResponse(status = 400,
                                  entity = s"Rows not affected. Error Inserting Blog: $body").withHeaders(CORSHeaders)
          case _ =>
            logger.error(s"Unknown Rows Affected Inserting new Blog $body")
            client ! HttpResponse(status = 400, entity = s"Unknown not affected. Error Inserting Blog: $body")
                                  .withHeaders(CORSHeaders)
        }).recover {
        case ex: TimeoutException =>
          logger.error(s"Timeout Inserting new Blog $body ${ex.getMessage} $ex")
          client ! HttpResponse(status = 500, entity = s"Timeout Inserting Blog: $body").withHeaders(CORSHeaders)
        case _ =>
          logger.error(s"Unknown Error Inserting new Blog $body")
          client ! HttpResponse(status = 500, entity = s"Unknown Error Inserting Blog: $body").withHeaders(CORSHeaders)
      }

    case HttpRequest(GET, Uri.Path("/ping"), _, _, _) =>
      sender ! HttpResponse(entity = "PONG!").withHeaders(CORSHeaders)

    case unhandledRequest : HttpRequest =>
      logger.error(s"Unknown Resource Requested ${unhandledRequest.method} ${unhandledRequest.uri}")
      sender ! HttpResponse(status = 404, entity = "Unknown resource!")
                            .withHeaders(CacheHeader(MaxAge404)).withHeaders(CORSHeaders)
  }

}
