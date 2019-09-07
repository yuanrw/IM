#!/bin/bash
SERVICE_NAME="client-samples"
VERSION="1.0.0"

LOG_DIR=/tmp/IM_logs
mkdir -p $LOG_DIR

# Find Java
if [[ -n "$JAVA_HOME" ]] && [[ -x "$JAVA_HOME/bin/java" ]]; then
    java="$JAVA_HOME/bin/java"
elif type -p java > /dev/null 2>&1; then
    java=$(type -p java)
elif [[ -x "/usr/bin/java" ]];  then
    java="/usr/bin/java"
else
    echo "Unable to find Java"
    exit 1
fi

JAVA_OPTS="-Xms512m -Xmx512m -Xmn256m -XX:PermSize=128m -XX:MaxPermSize=128m"

echo "JAVA_HOME: $JAVA_HOME"
$java $JAVA_OPTS -jar $SERVICE_NAME-$VERSION.jar connector http://rest-web:8082
echo "SERVICE_NAME started...."