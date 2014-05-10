@ECHO OFF
SETLOCAL
REM ###########################################################################
REM #        (c) Copyright, Real-Time Innovations, All rights reserved.       #
REM #                                                                         #
REM #        Permission to modify and use for internal purposes granted.      #
REM # This software is provided "as is", without warranty, express or implied #
REM #                                                                         #
REM ###########################################################################


REM If Java compiler is not in your search path, set it here:
REM SET JAVA="C:\Program Files\Java\jdk1.7.0_45\bin\java.exe"


REM Make sure NDDSHOME is set correctly
IF NOT DEFINED NDDSHOME (
    ECHO "NDDSHOME environment variable is not set"
    GOTO ENDSCRIPT
)
SET NDDSHOME_NQ=%NDDSHOME:"=%

REM Attempt to set Path from which to load native libraries.
REM If RTI_EXAMPLE_ARCH is set (e.g. to i86Win32jdk), you don't have to
REM separately set the Path.
IF NOT DEFINED RTI_EXAMPLE_ARCH (
    GOTO SKIP_SET_PATH
)
set Path=%NDDSHOME_NQ%\lib\%RTI_EXAMPLE_ARCH%;%Path%
:SKIP_SET_PATH

IF DEFINED JAVA (
    GOTO SKIP_JAVA_CHECK
)
SET JAVA=java.exe
REM Make sure java is in the search path
SET PATH_NQ=%PATH:"=%
FOR %%F IN (java.exe) DO IF NOT EXIST %%~$PATH_NQ:F (
    ECHO Error: java.exe not found in current search path.
    ECHO Make sure that JRE is correctly installed and that you have
    ECHO java.exe in your search path.
    GOTO ENDSCRIPT
)
:SKIP_JAVA_CHECK

REM Ensure the software has been built
IF NOT EXIST objs (
    ECHO Binary directory not found. Did you build the application?
    GOTO ENDSCRIPT
)

%JAVA% -classpath objs;"%NDDSHOME_NQ%\class\nddsjava.jar" com.rti.simple.HelloSubscriber %1 %2 %3 %4 %5 %6


:ENDSCRIPT:
