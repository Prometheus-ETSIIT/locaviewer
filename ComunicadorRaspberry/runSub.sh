#!/bin/sh
###############################################################################
##         (c) Copyright, Real-Time Innovations, All rights reserved.        ##
##                                                                           ##
##         Permission to modify and use for internal purposes granted.       ##
## This software is provided "as is", without warranty, express or implied.  ##
##                                                                           ##
###############################################################################

# You can override the following settings with the correct location of Java
JAVA=`which java`

# Make sure JAVA and NDDSHOME are set correctly
test -z "$JAVA" && echo "java not found" && exit 0
test -z "$NDDSHOME" && echo "NDDSHOME environment variable not set!" && exit 0

# Attempt to set LD_LIBRARY_PATH from which to load native libraries.
# If RTI_EXAMPLE_ARCH is set (e.g. to i86Linux2.6gcc4.1.1jdk), you don't
# have to separately set the LD_LIBRARY_PATH (or DYLD_LIBRARY_PATH in
# Darwin).
if [ `uname` = "Darwin" ]; then
   DYLD_LIBRARY_PATH=${NDDSHOME}/lib/${RTI_EXAMPLE_ARCH}:${DYLD_LIBRARY_PATH}
   export DYLD_LIBRARY_PATH
else
   LD_LIBRARY_PATH=${NDDSHOME}/lib/${RTI_EXAMPLE_ARCH}:${LD_LIBRARY_PATH}
   export LD_LIBRARY_PATH
fi

# Ensure this script is invoked from the root directory of the project
test ! -d src && echo "You must run this script from the example root directory" && exit 0

# Run example
$JAVA -classpath objs:"$NDDSHOME/class/nddsjavad.jar" com.rti.simple.HelloSubscriber "$@"
