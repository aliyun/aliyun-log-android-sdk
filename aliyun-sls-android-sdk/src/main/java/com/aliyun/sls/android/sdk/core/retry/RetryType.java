package com.aliyun.sls.android.sdk.core.retry;

/**
 * Created by zhouzhuo on 9/19/15.
 */
public enum RetryType {
    RetryTypeShouldNotRetry,
    RetryTypeShouldRetry,
    RetryTypeShouldFixedTimeSkewedAndRetry,
}
