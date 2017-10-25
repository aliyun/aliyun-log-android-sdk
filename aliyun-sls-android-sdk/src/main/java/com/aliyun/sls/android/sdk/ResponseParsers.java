package com.aliyun.sls.android.sdk;

import com.aliyun.sls.android.sdk.core.parser.AbstractResponseParser;
import com.aliyun.sls.android.sdk.result.PostLogResult;

import okhttp3.Response;

/**
 * Created by zhuoqin on 10/18/17.
 */
public final class ResponseParsers {

    public static class PostLogResponseParser extends AbstractResponseParser<PostLogResult> {

        @Override
        public PostLogResult parseData(Response response,PostLogResult result) throws Exception{
            return result;
        }
    }


}
