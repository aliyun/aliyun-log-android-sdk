#include <string.h>
#include <libs/include/log_http_interface.h>

extern "C" {
#include <jni.h>
#include <android/looper.h>

static jclass cls_foo = NULL;
static jmethodID mid_http_post = NULL;
JavaVM *g_VM;
ALooper *main_thread_looper;
static jmethodID http_response_get_status_code;
static jmethodID http_response_get_request_id;
static jmethodID http_response_get_error_message;

int os_http_post(const char *url,
                 char **header_array,
                 int header_count,
                 const void *data,
                 int data_len,
                 post_log_result *http_response) {
    if (mid_http_post == NULL || cls_foo == NULL)
        return 400;

    if (url == NULL || *url == 0 || header_array == NULL || header_count < 1 || data == NULL ||
        data_len <= 0)
        return 400;

    JNIEnv *env;
    g_VM->AttachCurrentThread(&env, NULL);
    if (g_VM->AttachCurrentThread(&env, NULL) != JNI_OK) {
        return 400;
    }
    jstring jurl = env->NewStringUTF(url);

    jobjectArray header = env->NewObjectArray(header_count * 2, env->FindClass("java/lang/String"),
                                              0);
    for (int i = 0; i < header_count; i++) {
        char *kv = header_array[i];
        if (kv != NULL) {
            char *eq = strchr(kv, ':');
            if (eq != NULL && eq != kv && eq[1] != 0) {
                *eq = 0;
                jstring key = env->NewStringUTF(kv);
                jstring val = env->NewStringUTF(eq + 1);
                *eq = '='; // restore
                env->SetObjectArrayElement(header, 2 * i, key);
                env->SetObjectArrayElement(header, 2 * i + 1, val);
                env->DeleteLocalRef(key);
                env->DeleteLocalRef(val);
            }
        }
    }

    jbyteArray body = env->NewByteArray(data_len);
    env->SetByteArrayRegion(body, 0, data_len, static_cast<const jbyte *>(data));

    jobject log_http_response = env->CallStaticObjectMethod(cls_foo, mid_http_post, jurl, header, body);

    jint res = env->CallIntMethod(log_http_response, http_response_get_status_code);
    jstring request_id = (jstring)env->CallObjectMethod(log_http_response, http_response_get_request_id);
    jstring error_message = (jstring)env->CallObjectMethod(log_http_response, http_response_get_error_message);
    const char *request_id_str = env->GetStringUTFChars(request_id, 0);
    const char *error_message_str = env->GetStringUTFChars(error_message, 0);

    http_response->statusCode = res;
    http_response->requestID = (char *)malloc(strlen(request_id_str) + 1);
    strcpy(http_response->requestID, request_id_str);

    http_response->errorMessage = (char *)malloc(strlen(error_message_str) + 1);
    strcpy(http_response->errorMessage, error_message_str);

    env->ReleaseStringUTFChars(request_id, request_id_str);
    env->ReleaseStringUTFChars(error_message, error_message_str);

    env->DeleteLocalRef(jurl);
    env->DeleteLocalRef(header);
    env->DeleteLocalRef(body);
    g_VM->DetachCurrentThread();
    env = NULL;
    return res;
}

jint JNI_OnLoad(JavaVM *vm, void *reserved) {
    (void) reserved;
    JNIEnv *env = NULL;
    jint result = -1;
    if (vm->GetEnv((void **) &env, JNI_VERSION_1_6) != JNI_OK) {
        return -1;
    }
    g_VM = vm;
    main_thread_looper = ALooper_forThread();

    jclass cls = env->FindClass("com/aliyun/sls/android/producer/LogProducerHttpTool");
    cls_foo = (jclass) env->NewGlobalRef(cls);
    if (cls_foo != NULL) {
        mid_http_post = env->GetStaticMethodID(cls_foo, "android_http_post",
                                               "(Ljava/lang/String;[Ljava/lang/String;[B)Lcom/aliyun/sls/android/producer/LogHttpResponse;");
    }

    log_set_http_post_func(os_http_post);

    jclass log_http_response_class = env->FindClass("com/aliyun/sls/android/producer/LogHttpResponse");
    http_response_get_status_code = env->GetMethodID(log_http_response_class, "getStatusCode", "()I");
    http_response_get_request_id = env->GetMethodID(log_http_response_class, "getRequestId", "()Ljava/lang/String;");
    http_response_get_error_message = env->GetMethodID(log_http_response_class, "getErrorMessage", "()Ljava/lang/String;");

    result = JNI_VERSION_1_4;
    return result;
}

}
