#!/bin/bash
cd build/classes
cp ../../src/gava/USER_QOS_PROFILES.xml ./
cp ../../src/gava/InfoCamaras.xml ./
cp ../../../rti_license.dat ./

if [ $IS_RASPBERRY_PI ]; then
  export RTI_ARCH=armv6vfphLinux3.xgcc4.7.2jdk
  export RTI_ARCH_WAN=armv7aQNX6.5.0SP1qcc_cpp4.4.2
else
  export RTI_ARCH=x64Linux2.6gcc4.4.5jdk
  export RTI_ARCH_WAN=x64Linux2.6gcc4.1.1
fi

source $RTI_CONNEXT_PATH/rti_set_bash_5.1.0
export PATH=$RTI_CONNEXT_PATH/openssl-1.0.1g/$RTI_ARCH_WAN/bin:$PATH
export LD_LIBRARY_PATH=$NDDSHOME/lib/$RTI_ARCH:$NDDSHOME/lib/$RTI_ARCH_WAN:$RTI_CONNEXT_PATH/openssl-1.0.1g/$RTI_ARCH_WAN/lib:$LD_LIBRARY_PATH

export GST_DEBUG_DUMP_DOT_DIR=$HOME
java -Djna.nosys=true -cp ../../../DDStheus/dist/DDStheus.jar:../../../Libs/gstreamer-java/gstreamer-java-1.6.jar:../../../Libs/JNA/jna-4.1.0.jar:../../../Libs/JNA/jna-platform-4.1.0.jar:$NDDSHOME/class/nddsjavad.jar:./ gava.Suscriptor $@
