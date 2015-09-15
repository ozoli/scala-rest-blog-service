package io.ozoli.blog

import io.ozoli.blog.util.AbstractApplicationDaemon

/**
 * Created by ocarr on 14/09/15.
 */
class BlogRestAppDaemon extends AbstractApplicationDaemon {
  def application = new BlogRestApp
}
