#!/bin/bash
cd bin
source /home/pi/RTI/rti_set_bash_5.1.0
sudo LD_LIBRARY_PATH=$NDDSHOME/lib/armv6vfphLinux3.xgcc4.7.2jdk java -cp ../../Libs/JavaOctave/javaoctave-0.6.4.jar:../../Libs/JavaOctave/commons-logging-1.1.3.jar:$NDDSHOME/class/nddsjavad.jar:./ comunicador.Servidor
 
