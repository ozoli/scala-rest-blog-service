package db

import akka.actor.Actor

case object GetBlogs
case class GetBlog(id: Long)

class DbHandler extends Actor with DB {

  def system = context.system

  override implicit def dispatcher = context.dispatcher

  val id = "id:(\\d)+".r

  /**
   * Writes incoming message to database and returns all data in db to user
   * @return
   */
  def receive = {
//    case GetBlogs => sender ! "hi to you too"
    case GetBlogs => sender ! getAllBlogEntries
//    case GetBlog(blogId: Long) => sender ! findBlogById(blogId)
    case _ => println("that was unexpected")
  }

}
