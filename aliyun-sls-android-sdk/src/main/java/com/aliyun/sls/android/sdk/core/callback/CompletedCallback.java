package com.aliyun.sls.android.sdk.core.callback;


import com.aliyun.sls.android.sdk.ClientException;
import com.aliyun.sls.android.sdk.LogException;
import com.aliyun.sls.android.sdk.core.Request;
import com.aliyun.sls.android.sdk.core.Result;
import com.aliyun.sls.android.sdk.ServiceException;

/**
 * Created by zhouzhuo on 11/19/15.
 */
public interface CompletedCallback<T1 extends Request, T2 extends Result> {

    public void onSuccess(T1 request, T2 result);

    public void onFailure(T1 request, LogException exception);
}
