#!/bin/sh
### BEGIN INIT INFO
# Provides:          blogRestService
# Required-Start:    $local_fs $remote_fs $network
# Required-Stop:     $local_fs $remote_fs $network
# Should-Start:      $named
# Should-Stop:       $named
# Default-Start:     2 3 4 5
# Default-Stop:      0 1 6
# Short-Description: Control ozoli blog REST app
# Description:       Control ozoli blog REST app daemon.
### END INIT INFO

./setEnv.sh

set -e

#if [ -z "${JAVA_HOME}" ]; then
#        JAVA_HOME=$(readlink -f /usr/bin/java | sed "s:/bin/java::")
#fi
JAVA_OPTS="-Xms512m -Xmx1024m"

APP=blogRestService

PID=/var/run/${APP}.pid
OUT_LOG=/var/log/${APP}/${APP}_out.log
ERR_LOG=/var/log/${APP}/${APP}_err.log

DAEMON_USER=root

APP_LOG_CONFIG=/etc/ozoli/${APP}_logback.xml
APP_CONFIG=/etc/ozoli/${APP}.conf
APP_HOME=/opt/${APP}
APP_CLASSPATH=$APP_HOME/${APP}.jar
APP_CLASS=io.ozoli.blog.BlogRestAppDaemon

if [ -n "$APP_LOG_CONFIG}" ]; then
        JAVA_OPTS="-Dlogback.configurationFile=${APP_LOG_CONFIG} ${JAVA_OPTS}"
fi

DAEMON_ARGS="-jvm server -Dconfig.file=${APP_CONFIG} ${JAVA_OPTS} -Djava.awt.headless=true -pidfile ${PID}"
DAEMON_ARGS="$DAEMON_ARGS -user ${DAEMON_USER} -outfile ${OUT_LOG} -errfile ${ERR_LOG}"
DAEMON_ARGS="$DAEMON_ARGS -cp ${APP_CLASSPATH} ${APP_CLASS}"

#. /lib/lsb/init-functions

case "$1" in
        start)
                echo "Starting ${APP}"
                cd ${APP_HOME} && jsvc ${DAEMON_ARGS}
                ;;
        stop)
                echo "Stopping ${APP}"
                cd ${APP_HOME} && jsvc -stop ${DAEMON_ARGS}
                ;;
        *)
                echo "Usage:  {start|stop}"
                exit 1
                ;;
esac

exit 0

