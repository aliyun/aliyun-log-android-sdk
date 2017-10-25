package com.aliyun.sls.android.sdk.core.parser;


import com.aliyun.sls.android.sdk.core.Result;

import java.io.IOException;

import okhttp3.Response;

/**
 * Created by zhouzhuo on 11/23/15.
 */
public interface ResponseParser<T extends Result> {

    public T parse(Response response) throws IOException;
}
