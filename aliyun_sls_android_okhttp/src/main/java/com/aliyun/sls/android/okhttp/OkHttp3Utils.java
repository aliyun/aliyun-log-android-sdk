package com.aliyun.sls.android.okhttp;

import com.aliyun.sls.android.ot.Span;
import com.aliyun.sls.android.ot.context.ContextManager;
import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;

/**
 * @author gordon
 * @date 2022/12/1
 */
public final class OkHttp3Utils {

    private OkHttp3Utils() {
        //no instance
    }

    public static Request newRequest(Request request) {
        Request.Builder builder = request.newBuilder();
        final Span span = ContextManager.INSTANCE.activeSpan();
        if (null != span) {
            builder.tag(Span.class, span);
        }
        return builder.build();
    }

    public static void registerTracerInterceptor(OkHttpClient.Builder builder) {
        builder.addInterceptor(new OKHttp3TracerInterceptor());
    }

    public Call newCall(Request request) {
        request = newRequest(request);
        return OKHttp3Tracer.newCallFactory(null).newCall(request);
    }
}
