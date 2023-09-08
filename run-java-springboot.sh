#!/bin/sh

cd ${APP_DIR}
exec java ${JAVA_OPTS}   -Dext.properties.file=file:./application.properties   -jar app.jar ${@}
