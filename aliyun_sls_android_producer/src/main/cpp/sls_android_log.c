//
// Created by gordon on 2021/9/23.
//

#include <libs/include/inner_log.h>
#include <android/log.h>

#ifndef __SLS_ANDROID_LOG__
#define __SLS_ANDROID_LOG__

#define SLS_LOG_TAG "sls_android_native"
#define SLS_LOGV(...) __android_log_print(ANDROID_LOG_VERBOSE, SLS_LOG_TAG, __VA_ARGS__)
#define SLS_LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, SLS_LOG_TAG, __VA_ARGS__)
#define SLS_LOGI(...) __android_log_print(ANDROID_LOG_INFO, SLS_LOG_TAG, __VA_ARGS__)
#define SLS_LOGW(...) __android_log_print(ANDROID_LOG_WARN, SLS_LOG_TAG, __VA_ARGS__)
#define SLS_LOGE(...) __android_log_print(ANDROID_LOG_ERROR, SLS_LOG_TAG, __VA_ARGS__)

#endif

void aos_print_log_android(int level, char *log)
{
    if (level == AOS_LOG_TRACE) {
        SLS_LOGV("%s", log);
    } else if (level == AOS_LOG_DEBUG) {
        SLS_LOGD("%s", log);
    } else if (level == AOS_LOG_INFO) {
        SLS_LOGI("%s", log);
    } else if (level == AOS_LOG_WARN) {
        SLS_LOGW("%s", log);
    } else if (level == AOS_LOG_ERROR) {
        SLS_LOGE("%s", log);
    }
}