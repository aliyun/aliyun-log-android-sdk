package com.aliyun.sls.android.sdk.core.http;


import com.aliyun.sls.android.sdk.utils.HttpHeaders;

public interface CommonHeaders extends HttpHeaders {



    static final String COMMON_HEADER_REQUEST_ID = "x-log-requestid";



    static final String COMMON_HEADER_SECURITY_TOKEN = "x-log-security-token";

}
