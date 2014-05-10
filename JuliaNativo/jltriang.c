#include <jni.h>
#include <stdio.h>
#include "jltriang.h"

JNIEXPORT jdoubleArray JNICALL Java_juliacomm_TriangulacionJulia_triangular
  (JNIEnv * env, jobject thisObj, jdoubleArray sensorX, jdoubleArray sensorY, jdoubleArray rssi)
{
    printf("Hello World\n");
}