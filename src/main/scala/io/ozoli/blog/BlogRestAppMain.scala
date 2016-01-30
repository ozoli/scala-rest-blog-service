package io.ozoli.blog

/**
 * Blog REST application object implementing main.
 */
object BlogRestAppMain extends BlogRestApp {

  def main(args:Array[String]) = {
    logger.info(s"Starting BlogRestApp")
    start()
  }
}
