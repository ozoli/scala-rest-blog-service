package io.ozoli.blog

import io.ozoli.blog.util.AbstractApplicationDaemon

/**
 * A process daemon for the Blog REST service
 */
class BlogRestAppDaemon extends AbstractApplicationDaemon {
  def application = new BlogRestApp
}
