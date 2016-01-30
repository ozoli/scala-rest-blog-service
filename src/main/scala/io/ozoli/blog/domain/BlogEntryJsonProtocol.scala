package io.ozoli.blog.domain

import java.time.format.DateTimeFormatter

import spray.json._

/**
 * Custom JSON deserialiser for BlogEntry's. Needed due to
 * the Joda Date Time field in BlogEntry.
 */
object BlogEntryJsonProtocol extends DefaultJsonProtocol {

  lazy val dateTimeFormatter : DateTimeFormatter = DateTimeFormatter.ofPattern("EEEE d MMMM yyyy HH:mm")

  implicit object BlogEntryJsonFormat extends RootJsonFormat[BlogEntry] {
    def write(blogEntry: BlogEntry) = JsObject(
      "pubDate" -> JsString(dateTimeFormatter.format(blogEntry.pubDate)),
      "title" -> JsString(blogEntry.title),
      "linkTitle" -> JsString(blogEntry.linkTitle),
      "body" -> JsString(blogEntry.body),
      "category" -> JsString(blogEntry.category)
    )

    def read(value: JsValue) = {
      throw new DeserializationException("Read BlogEntry Not Implemented")
    }
  }
}
