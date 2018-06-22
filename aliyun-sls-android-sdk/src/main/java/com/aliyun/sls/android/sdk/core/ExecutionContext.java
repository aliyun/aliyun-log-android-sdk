package com.aliyun.sls.android.sdk.core;

import com.aliyun.sls.android.sdk.core.callback.CompletedCallback;
import com.aliyun.sls.android.sdk.request.PostLogRequest;
import com.aliyun.sls.android.sdk.result.PostLogResult;

import java.lang.ref.WeakReference;
import java.util.WeakHashMap;

import okhttp3.OkHttpClient;

/**
 * Created by zhouzhuo on 11/22/15.
 */
public class ExecutionContext<T extends Request> {

    private T request;
    private OkHttpClient client;
    private CancellationHandler cancellationHandler = new CancellationHandler();

//    private CompletedCallback completedCallback;

    private WeakReference<CompletedCallback> completedCallback;

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

    public CompletedCallback getCompletedCallback() {
        return completedCallback.get();
    }

    public void setCompletedCallback(CompletedCallback completedCallback) {
        this.completedCallback = new WeakReference<CompletedCallback>(completedCallback);
//        this.completedCallback = completedCallback;
    }
}
