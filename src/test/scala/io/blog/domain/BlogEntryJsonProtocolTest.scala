package io.blog.domain

import io.ozoli.blog.domain.BlogEntryJsonProtocol
import org.scalatest.{Matchers, FlatSpec}
import spray.json.{JsBoolean, DeserializationException}

/**
 * Unit test of error case in {@link BlogEntryJsonProtocol}.
 */
class BlogEntryJsonProtocolTest extends FlatSpec with Matchers {

  an [DeserializationException] should be thrownBy BlogEntryJsonProtocol.BlogEntryJsonFormat.read(JsBoolean(true))

}
