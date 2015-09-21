package io.ozoli.blog

import grizzled.slf4j.Logger
import io.ozoli.blog.util.RssReader
import akka.actor._

import db._
import io.ozoli.blog.domain.BlogEntryJsonProtocol

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

import BlogEntryJsonProtocol._

/**
 * A RESTful web service for Blog Entries
 */
class BlogService extends Actor with ActorLogging with DB with SprayJsonSupport {

  private lazy val CacheHeader = (maxAge: Long) => `Cache-Control`(`max-age`(maxAge)) :: Nil
  private lazy val MaxAge = 2592000.toLong  // 30days (60 sec * 60 min * 24 hours * 30 days)
  private lazy val MaxAge404 = 600l

  private lazy val blogPerIdUri = "(/blog/)\\d+".r

  val logger = Logger[this.type]

  override def system = context.system

  override implicit def dispatcher: ExecutionContext = context.dispatcher

  override def receive = {
    // when a new connection comes in we register ourselves as the connection handler
    case _: Http.Connected => sender ! Http.Register(self)

    case HttpRequest(GET, Uri.Path("/blogs"), _, _, _) => {
      val client = sender()
      getAllBlogEntries.onComplete {
        case Success(result) =>
          client ! HttpResponse(status = 200).withEntity(
            HttpEntity(ContentTypes.`application/json`, result.toJson.compactPrint)).withHeaders(CacheHeader(MaxAge))
        case Failure(ex) =>
          logger.error(s"Error Finding Blogs ${ex.getMessage} ${ex}")
          client ! HttpResponse(status = 500, entity = "Error Finding Blogs").withHeaders(CacheHeader(MaxAge404))
      }
    }

    case HttpRequest(GET, Uri.Path(path), _, _, _)
      if path matches "(/blog/)\\d+" => {
        val client = sender()
        val blogId = "\\d+".r.findFirstIn(path)
        findBlogById(blogId.get.toInt).onComplete {
          case Success(queryResult) => {
            queryResult.get match {
              case resultSet if resultSet.nonEmpty =>
                client ! HttpResponse(status = 200).withEntity(
                  HttpEntity(ContentTypes.`application/json`, getData(resultSet.head).toJson.compactPrint))
                  .withHeaders(CacheHeader(MaxAge))
              case resultSet if resultSet.isEmpty =>
                logger.error(s"Error Finding Blog ID ${blogId.get.toInt}")
                client ! HttpResponse(status = 400,
                                      entity = "Blog ID Not Found").withHeaders(CacheHeader(MaxAge404))
              case _ =>
                logger.error(s"Unknown Error Finding Blog ID ${blogId.get.toInt}")
                client ! HttpResponse(status = 400,
                                      entity = "Unknown Error Finding Blog ID").withHeaders(CacheHeader(MaxAge404))
            }
          }
          case Failure(ex) =>
            logger.error(s"Error Finding Blog ID ${ex.getMessage} ${ex}")
            client ! HttpResponse(status = 500, entity = "Error Finding Blog ID").withHeaders(CacheHeader(MaxAge404))
        }
    }

    case HttpRequest(POST, Uri.Path("/blogs"), _, body, _) => {
      val blogEntries = RssReader.getBlogEntries(XML.loadString(body.asString))
      val client = sender()
      addBlogEntry(blogEntries.head).map(
        queryResult => queryResult.rowsAffected match {
          case 1 => client ! HttpResponse(entity = "OK")
          case 0 =>
            logger.error(s"Zero Rows Affected Inserting new Blog ${blogEntries.head}")
            client ! HttpResponse(status = 400,
                                  entity = "Rows not affected. Error Inserting Blog: %s".format(blogEntries.head))
          case _ =>
            logger.error(s"Unknown Rows Affected Inserting new Blog ${blogEntries.head}")
            client ! HttpResponse(status = 400,
                                  entity = "Unknown not affected. Error Inserting Blog: %s".format(blogEntries.head))
        }).recover {
        case ex: TimeoutException =>
          logger.error(s"Timeout Inserting new Blog ${blogEntries.head} ${ex.getMessage} ${ex}")
          client ! HttpResponse(status = 500, entity = "Timeout Inserting Blog: %s".format(blogEntries.head))
        case _ =>
          logger.error(s"Unknown Error Inserting new Blog ${blogEntries.head}")
          client ! HttpResponse(status = 500, entity = "Unknown Error Inserting Blog: %s".format(blogEntries.head))
      }
    }

    case HttpRequest(GET, Uri.Path("/ping"), _, _, _) => sender ! HttpResponse(entity = "PONG!")

    case unhandledRequest : HttpRequest =>
      logger.error(s"Unknown Resource Requested ${unhandledRequest.method} ${unhandledRequest.uri}")
      sender ! HttpResponse(status = 404, entity = "Unknown resource!").withHeaders(CacheHeader(MaxAge404))
  }

}
