#!/bin/bash
cd src/comunicador/

source $RTI_CONNEXT_PATH/rti_set_bash_5.1.0
javac -cp ../../../Libs/JavaOctave/javaoctave-0.6.4.jar:../../../Libs/JavaOctave/commons-logging-1.1.3.jar:$NDDSHOME/class/nddsjavad.jar -encoding ISO-8859-1 *.java
mv *.class ../../bin/comunicador/
