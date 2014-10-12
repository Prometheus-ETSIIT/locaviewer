#!/bin/bash
cd src/comunicador/
javac -cp ../../../Libs/JavaOctave/javaoctave-0.6.4.jar:../../../Libs/JavaOctave/commons-logging-1.1.3.jar:/home/pi/RTI/ndds.5.1.0/class/nddsjavad.jar -encoding ISO-8859-1 *.java
mv *.class ../../bin/comunicador/
