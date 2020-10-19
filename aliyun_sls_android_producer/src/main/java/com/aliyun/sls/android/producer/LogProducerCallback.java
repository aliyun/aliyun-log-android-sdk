package com.aliyun.sls.android.producer;

public interface LogProducerCallback {
    public void onCall(int resultCode, String reqId, String errorMessage, int logBytes, int compressedBytes);
}
