package com.aliyun.sls.android.sdk;

import com.aliyun.sls.android.sdk.core.parser.AbstractResponseParser;
import com.aliyun.sls.android.sdk.result.PostLogResult;
import com.aliyun.sls.android.sdk.result.PostCachedLogResult;

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

    public static class PostCachedLogResponseParser extends AbstractResponseParser<PostCachedLogResult> {

        @Override
        public PostCachedLogResult parseData(Response response, PostCachedLogResult result) throws Exception{
            return result;
        }
    }

}
