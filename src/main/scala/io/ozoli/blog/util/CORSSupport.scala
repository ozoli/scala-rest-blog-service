package io.ozoli.blog.util

import spray.http.HttpHeaders._
import spray.http.HttpMethods.{GET, POST, OPTIONS}

/**
 * CORSSupport directive adapted from https://github.com/wesovi/spray-cors-demo
 */
trait CORSSupport {

  lazy val CORSHeadersOptionsBlogs = List(
    RawHeader("Access-Control-Allow-Origin", "*"),
    `Access-Control-Allow-Methods`(GET, POST, OPTIONS),
    `Access-Control-Allow-Headers`("X-Accept-Charset,X-Accept,X-Requested-With,Content-Type,Accept,Origin,Access-Control-Request-Method,Access-Control-Request-Headers,Accept-Encoding,Accept-Language"),
    RawHeader("Vary", "Origin")
  )

  lazy val CORSHeaders = List(
    RawHeader("Access-Control-Allow-Origin", "*"),
    `Access-Control-Allow-Methods`(GET, POST, OPTIONS),
    `Access-Control-Allow-Headers`("X-Accept-Charset,X-Accept,X-Requested-With,Content-Type,Accept,Origin,Access-Control-Request-Method,Access-Control-Request-Headers,Accept-Encoding,Accept-Language"),
    RawHeader("Vary", "Origin")
  )

}
