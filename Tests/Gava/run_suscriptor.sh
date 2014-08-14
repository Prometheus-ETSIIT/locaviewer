#!/bin/bash
cd build/classes
LD_LIBRARY_PATH=/home/benito/Programas/RTI/ndds.5.1.0/lib/x64Linux2.6gcc4.4.5jdk java -cp ../../../../Libs/gstreamer-java/gstreamer-java-1.6.jar:../../../../Libs/JNA/jna-4.1.0.jar:../../../../Libs/JNA/jna-platform-4.1.0.jar:/home/benito/Programas/RTI/ndds.5.1.0/class/nddsjavad.jar:./ gava.Suscriptor