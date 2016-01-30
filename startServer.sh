#!/bin/sh
### BEGIN INIT INFO
# Provides:          blogRestService
# Required-Start:    $local_fs $remote_fs $network
# Required-Stop:     $local_fs $remote_fs $network
# Should-Start:      $named
# Should-Stop:       $named
# Default-Start:     2 3 4 5
# Default-Stop:      0 1 6
# Short-Description: Control yourid blog REST app
# Description:       Control yourid blog REST app daemon.
### END INIT INFO

./setEnv.sh

set -e

APP=blogRestService

APP_LOG_CONFIG=/etc/yourid/${APP}_logback.xml
APP_CONFIG=/etc/yourid/${APP}.conf
APP_HOME=/Users/yourid/local/bin/
APP_NAME=./blog-rest-app-main

JAVA_HOME=$(/usr/libexec/java_home -v 1.8)
JAVA_OPTS="-Xms512m -Xmx1024m -Dblog.rss.uri=http://your.feed.host/YourBlog?fmt=xml -Djava.awt.headless=true"
JAVA_OPTS="-Dlogback.configurationFile=/Users/yourid/local/etc/blogRestService_logback.xml ${JAVA_OPTS}"

PID=/Users/yourid/local/${APP}.pid

#. /lib/lsb/init-functions

case "$1" in
        start)
                echo "Starting ${APP}"
                cd ${APP_HOME} 
                ${APP_NAME} & echo $! >> ${PID}
                ;;
        stop)
                echo "Stopping ${APP}"
                kill -9 `cat ${PID}` && rm ${PID}
                ;;
        *)
                echo "Usage: {start|stop}"
                exit 1
                ;;
esac

exit 0

