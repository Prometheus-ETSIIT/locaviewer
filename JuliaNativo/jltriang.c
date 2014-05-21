#include <jni.h>
#include <stdio.h>
#include <julia.h>
#include "jltriang.h"

JNIEXPORT jdoubleArray JNICALL Java_juliacomm_TriangulacionJulia_triangular
  (JNIEnv * env, jobject thisObj, jdoubleArray sensorX_JNI, 
        jdoubleArray sensorY_JNI, jdoubleArray rssi_JNI)
{
    // Convierte los vectores a tipo de C
    jdouble* sensorX_C = (*env)->GetDoubleArrayElements(env, sensorX_JNI, NULL);
    jdouble* sensorY_C = (*env)->GetDoubleArrayElements(env, sensorY_JNI, NULL);
    jdouble* rssi_C    = (*env)->GetDoubleArrayElements(env, rssi_JNI,    NULL);
    jsize sensorX_size = (*env)->GetArrayLength(env, sensorX_JNI); 
    jsize sensorY_size = (*env)->GetArrayLength(env, sensorY_JNI);
    jsize rssi_size    = (*env)->GetArrayLength(env, rssi_JNI);
    
    // Llama el script de julia
    printf("[C] A trabajar :D\n");
    
    // Inicialización
    jl_init("/home/benito/Programas/julia/");
    //JL_SET_STACK_BASE;
    
    // Convierte los valores a tipo Julia
    jl_value_t* array_type = jl_apply_array_type(jl_float64_type, 1);
    jl_array_t* sensorX_JL = jl_ptr_to_array_1d(array_type, sensorX_C, sensorX_size, 0);
    jl_array_t* sensorY_JL = jl_ptr_to_array_1d(array_type, sensorY_C, sensorY_size, 0);
    jl_array_t* rssi_JL    = jl_ptr_to_array_1d(array_type, rssi_C,    rssi_size,    0);
    
    jl_function_t *func     = jl_get_function(jl_base_module, "reverse!");
    jl_array_t* posicion_JL = (jl_array_t*)jl_call1(func, (jl_value_t*)sensorX_JL);
    double* posicion_C      = (double*)jl_array_data(posicion_JL);
    
    printf("[C] Resultado> X: %.2f | Y: %.2f\n", posicion_C[0], posicion_C[1]);
    
    // Libera los recursos
    (*env)->ReleaseDoubleArrayElements(env, sensorX_JNI, sensorX_C, 0);
    (*env)->ReleaseDoubleArrayElements(env, sensorY_JNI, sensorY_C, 0);
    (*env)->ReleaseDoubleArrayElements(env, rssi_JNI,    rssi_C,    0);
    
    // Devuelve el resultado
    jdoubleArray posicion_JNI = (*env)->NewDoubleArray(env, 2);
    (*env)->SetDoubleArrayRegion(env, posicion_JNI, 0 , 2, posicion_C);
    return posicion_JNI;
}