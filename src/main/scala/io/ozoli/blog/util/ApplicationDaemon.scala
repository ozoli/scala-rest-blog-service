package io.ozoli.blog.util

import org.apache.commons.daemon._

trait ApplicationLifecycle {
  def start(): Unit
  def stop(): Unit
}

/**
 * Daemon abstract call to init, start, stop and destroy an application.
 */
abstract class AbstractApplicationDaemon extends Daemon {
  def application: ApplicationLifecycle

  def init(daemonContext: DaemonContext) {}

  def start() = application.start()

  def stop() = application.stop()

  def destroy() = application.stop()
}
