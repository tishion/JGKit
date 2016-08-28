//
// Created by tishion on 8/27/2016.
//
#include <jni.h>

JNIEXPORT jstring JNICALL
Java_com_sheen_jgkit_ntv_Common_getVersion(JNIEnv *env, jclass type)
{
    // TODO
    return (*env)->NewStringUTF(env, "1.0.0.0");
}