package com.aliyun.sls.android.sdk.callback;


import com.aliyun.sls.android.sdk.ClientException;
import com.aliyun.sls.android.sdk.OSSRequest;
import com.aliyun.sls.android.sdk.OSSResult;
import com.aliyun.sls.android.sdk.ServiceException;

/**
 * Created by zhouzhuo on 11/19/15.
 */
public interface OSSCompletedCallback<T1 extends OSSRequest, T2 extends OSSResult> {

    public void onSuccess(T1 request, T2 result);

    public void onFailure(T1 request, ClientException clientException, ServiceException serviceException);
}
