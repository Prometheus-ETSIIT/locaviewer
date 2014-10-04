#!/bin/bash
cd build/classes
cp ../../../../rti_license.dat ./
cp ../../src/publicador/USER_QOS_PROFILES.xml ./

if [ $IS_RASPBERRY_PI ]; then
  export RTI_ARCH=armv6vfphLinux3.xgcc4.7.2jdk
else
  export RTI_ARCH=x64Linux2.6gcc4.4.5jdk
fi

source $RTI_CONNEXT_PATH/rti_set_bash_5.1.0
export LD_LIBRARY_PATH=$NDDSHOME/lib/$RTI_ARCH:$LD_LIBRARY_PATH
java -cp ../../../../DDStheus/dist/DDStheus.jar:$NDDSHOME/class/nddsjavad.jar:./ publicador.Program $@
