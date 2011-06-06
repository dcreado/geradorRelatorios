#!/bin/bash

DIRNAME=`dirname $0`


RUNJAR=$DIRNAME/geradorRelatorios.jar

CP=$DIRNAME/res/
CP=$CP:$DIRNAME/ojdbc5.jar

JAVA=$JAVA_HOME/bin/java

JAVA_OPTS="-Xms256m -Xmx1024m"

"$JAVA" $JAVA_OPTS -cp "$CP:$RUNJAR" br.com.cpqd.gees.relatorios.Main
