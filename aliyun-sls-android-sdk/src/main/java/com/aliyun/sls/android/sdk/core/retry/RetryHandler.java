package com.aliyun.sls.android.sdk.core.retry;


import android.text.TextUtils;

import com.aliyun.sls.android.sdk.LogException;
import com.aliyun.sls.android.sdk.SLSLog;

import java.io.InterruptedIOException;
import java.net.SocketTimeoutException;

/**
 * Created by zhouzhuo on 11/6/15.
 */
public class RetryHandler {

    private int maxRetryCount = 2;

    public void setMaxRetryCount(int maxRetryCount) {
        this.maxRetryCount = maxRetryCount;
    }

    public RetryHandler(int maxRetryCount) {
        setMaxRetryCount(maxRetryCount);
    }

    public RetryType shouldRetry(LogException e, int currentRetryCount) {

        if (e == null || currentRetryCount >= maxRetryCount) {
            return RetryType.RetryTypeShouldNotRetry;
        }

        if (e.canceled){
            return RetryType.RetryTypeShouldNotRetry;
        }

        //如果是因为网络原因，如果是服务器内部错误（httpcode > 500，需要尝试重试）
        String errorCode = e.getErrorCode();
        String errorMessage = e.getErrorMessage();

        if (e.responseCode >= 500){
            return RetryType.RetryTypeShouldRetry;
        }

//        //发起请求的时间和服务器时间超出15分钟,会要求重试，不知道SLS是否有这个逻辑
//        if (!TextUtils.isEmpty(errorMessage)){
//            if (errorMessage.contains("RequestTimeTooSkewed")){
//                return RetryType.RetryTypeShouldFixedTimeSkewedAndRetry;
//            }
//        }

        Exception localException = (Exception) e.getCause();
        if (localException instanceof InterruptedIOException
                && !(localException instanceof SocketTimeoutException)) {
            SLSLog.logError("[shouldRetry] - is interrupted!");
            return RetryType.RetryTypeShouldNotRetry;
        } else if (localException instanceof IllegalArgumentException) {
            return RetryType.RetryTypeShouldNotRetry;
        }

        return RetryType.RetryTypeShouldRetry;
    }
}
