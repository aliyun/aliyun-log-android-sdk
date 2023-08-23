//
// Created by gordon on 2022/9/20.
//
#include <libs/include/log_producer_config.h>
#include <jni.h>

#ifndef ALIYUN_LOG_ANDROID_SDK_SLS_ANDROID_HTTP_INJECT_H
#define ALIYUN_LOG_ANDROID_SDK_SLS_ANDROID_HTTP_INJECT_H

#define SXS_ANDROID_HTTP_INJECTOR_COUNT 20

/// Set http injector for producer
/// \param env
/// \param config
/// \param injector
void sls_set_android_http_inject(JNIEnv *env, jlong config, jobject injector);

/// The function of http injector
/// \param config
/// \param src_headers
/// \param src_count
/// \param dest_headers
/// \param dest_count
void sls_android_set_http_inject_func(log_producer_config *config, char **src_headers, int src_count, char **dest_headers, int *dest_count);


void sls_android_set_http_release_inject_func(__attribute__((unused)) log_producer_config *config, char **dest_headers, int dest_count);

/// Destroy http injector
__attribute__((unused)) void sls_destroy_http_injector();

#endif //ALIYUN_LOG_ANDROID_SDK_SLS_ANDROID_HTTP_INJECT_H
