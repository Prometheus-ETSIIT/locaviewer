#!/bin/bash
cd bin
cp ../../rti_license.dat ./

if [ $IS_RASPBERRY_PI ]; then
  export RTI_ARCH=armv6vfphLinux3.xgcc4.7.2jdk
else
  export RTI_ARCH=x64Linux2.6gcc4.4.5jdk
fi

source $RTI_CONNEXT_PATH/rti_set_bash_5.1.0
export LD_LIBRARY_PATH=$NDDSHOME/lib/$RTI_ARCH:$LD_LIBRARY_PATH

sudo java -cp ../../Libs/JavaOctave/javaoctave-0.6.4.jar:../../Libs/JavaOctave/commons-logging-1.1.3.jar:$NDDSHOME/class/nddsjavad.jar:./ comunicador.Sensor $@
