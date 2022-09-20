//
// Created by gordon on 2022/9/20.
//

#include <libs/include/log_http_interface.h>
#include "sls_android_http_inject.h"

#ifdef __cplusplus
extern "C" {
#endif

typedef struct _HttpHeaderInjectParams {
    log_producer_config *config;
    jobject injector;
} HttpHeaderInjectParams;

extern JavaVM *g_VM;
static HttpHeaderInjectParams _httpHeaderInjectParams[SXS_ANDROID_HTTP_INJECTOR_COUNT];

void sls_set_android_http_inject(JNIEnv *env, jlong config, jobject injector)
{
    if (NULL != injector) {
        HttpHeaderInjectParams headerInjectParams;
        headerInjectParams.config = (log_producer_config *) config;
        headerInjectParams.injector = (*env)->NewGlobalRef(env, injector);

        for (int i = 0; i < SXS_ANDROID_HTTP_INJECTOR_COUNT; ++i) {
            if (NULL != (*(_httpHeaderInjectParams+i)).config) {
                if ((*(_httpHeaderInjectParams+i)).config != (log_producer_config *) config) {
                    continue;
                }
            }

            *(_httpHeaderInjectParams+i) = headerInjectParams;
            break;
        }

        log_set_http_header_inject_func(sls_android_set_http_inject_func);
        log_set_http_header_release_inject_func(sls_android_set_http_release_inject_func);
    }
}

void sls_android_set_http_inject_func(log_producer_config *config, char **src_headers, int src_count, char **dest_headers, int *dest_count) {
    for (int i = 0; i < SXS_ANDROID_HTTP_INJECTOR_COUNT; ++i) {
        if (NULL == _httpHeaderInjectParams[i].config) {
            break;
        }
        if (config == _httpHeaderInjectParams[i].config) {
            jobject jcallback = _httpHeaderInjectParams[i].injector;

            if (NULL == g_VM) {
                return;
            }

            JNIEnv *env = NULL;
            if ((*g_VM)->AttachCurrentThread(g_VM, &env, NULL) != JNI_OK) {
                return;
            }

            jclass java_class = (*env)->GetObjectClass(env, jcallback);
            if (java_class == 0) {
                (*g_VM)->DetachCurrentThread(g_VM);
                return;
            }

            jmethodID java_callback_id = (*env)->GetMethodID(env, java_class,
                                                             "injectHeaders",
                                                             "([Ljava/lang/String;I)[Ljava/lang/String;");
            if (java_callback_id == NULL) {
                return;
            }

            jobjectArray headers = (*env)->NewObjectArray(env, src_count * 2, (*env)->FindClass(env, "java/lang/String"), 0);
            for (int j = 0; j < src_count; ++j) {
                char *kv = src_headers[j];
                if (NULL == kv) {
                    continue;
                }
                char *eq = strchr(kv, ':');
                if (NULL == eq || eq == kv || eq[1] == 0) {
                    continue;
                }

                *eq = 0;
                jstring key = (*env)->NewStringUTF(env, kv);
                jstring value = (*env)->NewStringUTF(env, eq + 1);
                *eq = '=';
                (*env)->SetObjectArrayElement(env, headers, 2 * j, key);
                (*env)->SetObjectArrayElement(env, headers, 2 * j + 1, value);
                (*env)->DeleteLocalRef(env, key);
                (*env)->DeleteLocalRef(env, value);
            }

            jobjectArray result_headers = (*env)->CallObjectMethod(env, jcallback, java_callback_id, headers, src_count);
            jsize count = (*env)->GetArrayLength(env, result_headers) / 2;

            for (int k = 0; k < count; ++k) {
                jstring key = (*env)->GetObjectArrayElement(env, result_headers, 2 * k);
                jstring value = (*env)->GetObjectArrayElement(env, result_headers, 2 * k + 1);
                if (NULL == key || NULL == value) {
                    (*env)->DeleteLocalRef(env, key);
                    (*env)->DeleteLocalRef(env, value);
                    continue;
                }

                const char *c_key = (*env)->GetStringUTFChars(env, key, NULL);
                const char *c_value = (*env)->GetStringUTFChars(env, value, NULL);
                char *kv = (char *) malloc(sizeof(char) * 256);
                memset(kv, 0, 256);
                strcat(kv, c_key);
                strcat(kv, ":");
                strcat(kv, c_value);

                dest_headers[k] = kv;
                (*dest_count) ++;
                (*env)->ReleaseStringUTFChars(env, key, c_key);
                (*env)->ReleaseStringUTFChars(env, value, c_value);
                (*env)->DeleteLocalRef(env, key);
                (*env)->DeleteLocalRef(env, value);
            }

            (*g_VM)->DetachCurrentThread(g_VM);
            env = NULL;
        }
    }
}

void sls_android_set_http_release_inject_func(__attribute__((unused)) log_producer_config *config, char **dest_headers, int dest_count)
{
    if (NULL == dest_headers) {
        return;
    }

    for (int i = 0; i < dest_count; ++i) {
        free(dest_headers[i]);
    }
}

__attribute__((unused))
void sls_destroy_http_injector()
{
    JNIEnv *env = NULL;
    if ((*g_VM)->AttachCurrentThread(g_VM, &env, NULL) != JNI_OK) {
        return;
    }

    for (int i = 0; i < SXS_ANDROID_HTTP_INJECTOR_COUNT; ++i) {
        if (NULL == _httpHeaderInjectParams[i].config || NULL == _httpHeaderInjectParams[i].injector) {
            continue;
        }

        (*env)->DeleteGlobalRef(env, _httpHeaderInjectParams[i].injector);
    }

    (*g_VM)->DetachCurrentThread(g_VM);
    env = NULL;
}

#ifdef __cplusplus
}
#endif