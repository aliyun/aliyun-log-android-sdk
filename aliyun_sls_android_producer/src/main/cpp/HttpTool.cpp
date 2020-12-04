#include <string.h>
#include <libs/include/log_http_interface.h>

extern "C" {
#include <jni.h>

static jclass cls_foo = NULL;
static jmethodID mid_http_post = NULL;
static JavaVM *g_VM;

int os_http_post(const char *url,
                 char **header_array,
                 int header_count,
                 const void *data,
                 int data_len) {
    if (mid_http_post == NULL || cls_foo == NULL)
        return 400;

    if (url == NULL || *url == 0 || header_array == NULL || header_count < 1 || data == NULL ||
        data_len <= 0)
        return 400;

    JNIEnv *env;
    g_VM->AttachCurrentThread(&env, NULL);
    if (g_VM->AttachCurrentThread(&env, NULL) != JNI_OK) {
        return 412;
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

    int res = env->CallStaticIntMethod(cls_foo, mid_http_post, jurl, header, body);

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

    jclass cls = env->FindClass("com/aliyun/sls/android/producer/LogProducerHttpTool");
    cls_foo = (jclass) env->NewGlobalRef(cls);
    if (cls_foo != NULL) {
        mid_http_post = env->GetStaticMethodID(cls_foo, "android_http_post",
                                               "(Ljava/lang/String;[Ljava/lang/String;[B)I");
    }

    log_set_http_post_func(os_http_post);

    result = JNI_VERSION_1_4;
    return result;
}

}
