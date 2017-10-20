package com.aliyun.sls.android.sdk.common;

/**
 * Miscellaneous constants used for oss client service.
 */
public final class Constants {

    public static final String SDK_VERSION = "2.4.3";
    public static final String DEFAULT_OSS_ENDPOINT = "http://oss-cn-hangzhou.aliyuncs.com";

    public static final String DEFAULT_CHARSET_NAME = "utf-8";
    public static final String DEFAULT_XML_ENCODING = "utf-8";

    public static final String DEFAULT_OBJECT_CONTENT_TYPE = "application/octet-stream";

    public static final int KB = 1024;
    public static final int DEFAULT_BUFFER_SIZE = 8 * KB;
    public static final int DEFAULT_STREAM_BUFFER_SIZE = 128 * KB;

    public static final int DEFAULT_RETRY_COUNT = 2;
    public static final int DEFAULT_BASE_THREAD_POOL_SIZE = 5;
    public static final int DEFAULT_EXTEND_THREAD_POOL_SIZE = 3;

    public static final long DEFAULT_FILE_SIZE_LIMIT = 5 * 1024 * 1024 * 1024L;

    public static final long MIN_PART_SIZE_LIMIT = 100 * KB;

    public static final String RESOURCE_NAME_COMMON = "common";
    public static final String RESOURCE_NAME_OSS = "oss";

    public static final int OBJECT_NAME_MAX_LENGTH = 1024;
}
