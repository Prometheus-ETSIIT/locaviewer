#!/bin/bash
cd build/classes
cp ../../src/gava/USER_QOS_PROFILES.xml ./
cp ../../src/gava/InfoCamaras.xml ./
cp ../../../rti_license.dat ./

if [ $IS_RASPBERRY_PI ]; then
  export RTI_ARCH=armv6vfphLinux3.xgcc4.7.2jdk
else
  export RTI_ARCH=x64Linux2.6gcc4.4.5jdk
fi

source $RTI_CONNEXT_PATH/rti_set_bash_5.1.0
export LD_LIBRARY_PATH=$NDDSHOME/lib/$RTI_ARCH:$LD_LIBRARY_PATH

export GST_DEBUG_DUMP_DOT_DIR=$HOME
java -cp ../../../DDStheus/dist/DDStheus.jar:../../../Libs/gstreamer-java/gstreamer-java-1.6.jar:../../../Libs/JNA/jna-4.1.0.jar:../../../Libs/JNA/jna-platform-4.1.0.jar:$NDDSHOME/class/nddsjavad.jar:./ gava.Publicador $@
