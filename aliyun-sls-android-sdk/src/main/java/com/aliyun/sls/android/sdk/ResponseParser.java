package com.aliyun.sls.android.sdk;


import java.io.IOException;

import okhttp3.Response;

/**
 * Created by zhouzhuo on 11/23/15.
 */
public interface ResponseParser<T extends OSSResult> {

    public T parse(Response response) throws IOException;
}
