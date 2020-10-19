LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)
LOCAL_MODULE := curl
LOCAL_SRC_FILES := libs/curl/$(TARGET_ARCH_ABI)/libcurl.a
LOCAL_EXPORT_CFLAGS := -Icurl/include
include $(PREBUILT_STATIC_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE := aliyun_log_c_sdk
LOCAL_SRC_FILES := libs/producer/$(TARGET_ARCH_ABI)/libaliyun_log_c_sdk.a
LOCAL_EXPORT_CFLAGS := -I$(LOCAL_PATH)/libs/producer/include
include $(PREBUILT_STATIC_LIBRARY)

include $(CLEAR_VARS)
LOCAL_C_INCLUDES :=
LOCAL_MODULE := sls_producer
LOCAL_SRC_FILES := com_aliyun_sls_android_producer_LogProducerClient.c com_aliyun_sls_android_producer_LogProducerConfig.c
LOCAL_STATIC_LIBRARIES := libcurl libaliyun_log_c_sdk
LOCAL_LDLIBS := -lz -llog
include $(BUILD_SHARED_LIBRARY)
