package com.aliyun.sls.android.sdk;

import com.aliyun.sls.android.sdk.callback.OSSCompletedCallback;
import com.aliyun.sls.android.sdk.callback.OSSProgressCallback;

import okhttp3.OkHttpClient;

/**
 * Created by zhouzhuo on 11/22/15.
 */
public class ExecutionContext<T extends OSSRequest> {

    private T request;
    private OkHttpClient client;
    private CancellationHandler cancellationHandler = new CancellationHandler();

    private OSSCompletedCallback completedCallback;
    private OSSProgressCallback progressCallback;

    public ExecutionContext(OkHttpClient client, T request) {
        setClient(client);
        setRequest(request);
    }

    public T getRequest() {
        return request;
    }

    public void setRequest(T request) {
        this.request = request;
    }

    public OkHttpClient getClient() {
        return client;
    }

    public void setClient(OkHttpClient client) {
        this.client = client;
    }

    public CancellationHandler getCancellationHandler() {
        return cancellationHandler;
    }

    public OSSCompletedCallback getCompletedCallback() {
        return completedCallback;
    }

    public void setCompletedCallback(OSSCompletedCallback completedCallback) {
        this.completedCallback = completedCallback;
    }

    public OSSProgressCallback getProgressCallback() {
        return progressCallback;
    }

    public void setProgressCallback(OSSProgressCallback progressCallback) {
        this.progressCallback = progressCallback;
    }
}
