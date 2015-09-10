package io.ozoli.blog

import db._
import spray.http.CacheDirectives.`max-age`
import spray.http.HttpHeaders.`Cache-Control`

import scala.concurrent._
import scala.concurrent.duration._
import akka.util.Timeout
import akka.actor._
import spray.can.Http
import spray.http._
import HttpMethods._

import akka.actor.{Actor, Props}

import scala.util.{Failure, Success}
import scala.xml.XML


class DemoService extends Actor with ActorLogging with DB {
  implicit val timeout: Timeout = 10.seconds // for the actor 'asks' // ExecutionContext for the futures and scheduler

  val CacheHeader = (maxAge: Long) => `Cache-Control`(`max-age`(maxAge)) :: Nil
  val MaxAge = 2592000.toLong  // 30days (60sec * 60min * 24hours * 30days)
  val MaxAge404 = 600l

  val blogPerIdUri = "(/blog/)\\d+".r

  override def system = context.system

  override implicit def dispatcher: ExecutionContext = context.dispatcher

  override def receive = {
    // when a new connection comes in we register ourselves as the connection handler
    case _: Http.Connected => sender ! Http.Register(self)

    case HttpRequest(GET, Uri.Path("/blogs"), _, _, _) => {
      val blogFuture: Future[String] = getAllBlogEntries
      val result = Await.result(blogFuture, timeout.duration)
      sender ! HttpResponse(status = 200).withEntity(
        HttpEntity(ContentTypes.`application/json`, result)).withHeaders(CacheHeader(MaxAge))
    }

    case HttpRequest(GET, Uri.Path(path), _, _, _)
      if path matches "(/blog/)\\d+" => {
      val client = sender()
      val blogId = "\\d+".r.findFirstIn(path)
      val futureBlogId = findBlogById(blogId.get.toInt)
      futureBlogId.onComplete {
        case Success(queryResult) => {
          queryResult.get match {
            case resultSet if resultSet.nonEmpty =>
              client ! HttpResponse(status = 200).withEntity(
                HttpEntity(ContentTypes.`application/json`, getData(resultSet.head))).withHeaders(CacheHeader(MaxAge))
            case resultSet if resultSet.isEmpty =>
              client ! HttpResponse(status = 400,
                                    entity = "Blog ID Not Found").withHeaders(CacheHeader(MaxAge404))
            case _ =>
              client ! HttpResponse(status = 400,
                                    entity = "Unknown Error Finding Blog ID").withHeaders(CacheHeader(MaxAge404))
          }
        }
        case Failure(ex) => {
          client ! HttpResponse(status = 500, entity = "Timeout Finding Blog ID").withHeaders(CacheHeader(MaxAge404))
        }
      }
    }

    case HttpRequest(POST, Uri.Path("/blogs"), _, body, _) => {
      val blogEntries = RssReader.getBlogEntries(XML.loadString(body.asString))
      val client = sender()
      addBlogEntry(blogEntries.head).map(
        queryResult => queryResult.rowsAffected match {
          case 1 => client ! HttpResponse(entity = "OK")
          case 0 => client ! HttpResponse(status = 400,
            entity = "Rows not affected. Error Inserting Blog: %s".format(blogEntries))
          case _ => client ! HttpResponse(status = 400,
            entity = "Unknown not affected. Error Inserting Blog: %s".format(blogEntries))
        }).recover {
        case ex: TimeoutException =>
          client ! HttpResponse(status = 500, entity = "Timeout Inserting Blog: %s".format(blogEntries))
        case _ => client ! HttpResponse(status = 500, entity = "Unknown Error Inserting Blog: %s".format(blogEntries))
      }
    }

    case HttpRequest(GET, Uri.Path("/ping"), _, _, _) => sender ! HttpResponse(entity = "PONG!")

    case _: HttpRequest =>
      sender ! HttpResponse(status = 404, entity = "Unknown resource!").withHeaders(CacheHeader(MaxAge404))
  }

}
