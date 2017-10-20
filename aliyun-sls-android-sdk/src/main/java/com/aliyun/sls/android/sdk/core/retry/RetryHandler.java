package com.aliyun.sls.android.sdk.core.retry;


import android.text.TextUtils;

import com.aliyun.sls.android.sdk.LogException;

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

        String errorCode = e.getErrorCode();
        String errorMessage = e.getErrorMessage();

        if (!TextUtils.isEmpty(errorCode)){
            if (Integer.valueOf(errorCode) >= 500){
                return RetryType.RetryTypeShouldRetry;
            }
        }

        if (!TextUtils.isEmpty(errorMessage)){
            if (errorMessage.contains("RequestTimeTooSkewed")){
                return RetryType.RetryTypeShouldFixedTimeSkewedAndRetry;
            }
        }

        return RetryType.RetryTypeShouldNotRetry;
    }
}
