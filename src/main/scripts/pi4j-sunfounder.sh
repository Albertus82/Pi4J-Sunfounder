#!/bin/sh
PRG="$0"
while [ -h "$PRG" ]; do
  ls=`ls -ld "$PRG"`
  link=`expr "$ls" : '.*-> \(.*\)$'`
  if expr "$link" : '/.*' > /dev/null; then
    PRG="$link"
  else
    PRG=`dirname "$PRG"`/"$link"
  fi
done
PRGDIR=`dirname "$PRG"`
java -Xms${java.xms}m -Xmx${java.xmx}m -XX:MaxMetaspaceSize=${java.MaxMetaspaceSize}m -XX:MaxPermSize=${java.MaxPermSize}m -XX:ReservedCodeCacheSize=${java.ReservedCodeCacheSize}m -classpath "$PRGDIR/${artifactId}.jar:$PRGDIR/*" it.albertus.pi4j.sunfounder.$1
