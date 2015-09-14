package io.ozoli.blog

import org.joda.time.format.{DateTimeFormat, DateTimeFormatter}
import spray.json._

/**
 * Custom JSON deserialiser for BlogEntry's. Needed due to
 * the Joda Date Time field in BlogEntry.
 */
object BlogEntryJsonProtocol extends DefaultJsonProtocol {

  lazy val dateTimeFormatter : DateTimeFormatter = DateTimeFormat.forPattern("ddd yyyy mm ss")

  implicit object BlogEntryJsonFormat extends RootJsonFormat[BlogEntry] {
    def write(blogEntry: BlogEntry) = JsObject(
      "id" -> JsNumber(blogEntry.id),
      "pubDate" -> JsString(dateTimeFormatter.print(blogEntry.pubDate)),
      "title" -> JsString(blogEntry.title),
      "body" -> JsString(blogEntry.body),
      "category" -> JsString(blogEntry.category)
    )
    def read(value: JsValue) = {
      throw new DeserializationException("Read BlogEntry Not Implemented")
    }
  }
}
