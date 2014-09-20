#!/bin/bash
cd build/classes
cp ../../src/gava/USER_QOS_PROFILES.xml ./
cp ../../rti_license.dat ./

export GST_DEBUG_DUMP_DOT_DIR=$HOME
source /home/benito/Programas/RTI/rti_set_bash_5.1.0
LD_LIBRARY_PATH=$NDDSHOME/lib/x64Linux2.6gcc4.4.5jdk java -cp ../../../Libs/gstreamer-java/gstreamer-java-1.6.jar:../../../Libs/JNA/jna-4.1.0.jar:../../../Libs/JNA/jna-platform-4.1.0.jar:$NDDSHOME/class/nddsjavad.jar:./ gava.Suscriptor $@