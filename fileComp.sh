#!/bin/sh

TMPz=`pwd`/`dirname $0`
APPHOME=`echo $TMPz | sed s:/bin::`


echo $APPHOME


java -jar $APPHOME/target/cl-file-comparator-1.0.0-SNAPSHOT-standalone.jar $*
