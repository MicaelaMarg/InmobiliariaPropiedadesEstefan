#!/bin/bash
set -e
# Railway inyecta PORT; Tomcat por defecto usa 8080.
PORT="${PORT:-8080}"
# Ajustar puerto en server.xml
sed -i "s/8080/${PORT}/g" "$CATALINA_HOME/conf/server.xml"
exec catalina.sh run
