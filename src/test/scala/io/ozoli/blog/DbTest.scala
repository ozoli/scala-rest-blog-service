package io.ozoli.blog

import com.github.simplyscala.{MongodProps, MongoEmbedDatabase}
import com.mongodb.casbah.{MongoCollection, MongoClient}
import com.mongodb.casbah.commons.MongoDBObject
import org.scalatest.{BeforeAndAfter, FunSuite}

/**
 * Unit test for the MongoDB persistence.
 */
class DbTest extends FunSuite with BeforeAndAfter with MongoEmbedDatabase {

  var mongoProps: MongodProps = null
  var blogCollection: MongoCollection = null

  before {
    mongoProps = mongoStart(port = 27017)
    val db = MongoClient()("ozoliblogdb")
    blogCollection = db("blogs")

    val builder = MongoDBObject.newBuilder
    builder += "title" -> "A New Blog Entry"
    builder += "body" -> "The text int the body that could go on and on..."

    blogCollection.insert(builder.result())
  }

  test("test find a blog") {
    val blog = blogCollection.find(MongoDBObject("title" -> "A New Blog Entry"))
    assert(blog.size == 1)
  }

  test("test find non existent blog") {
    val blog = blogCollection.find(MongoDBObject("title" -> "An Unknown Blog Entry"))
    assert(blog.size == 0)
  }

  after {
    blogCollection.drop()
    mongoStop(mongoProps)
  }
}