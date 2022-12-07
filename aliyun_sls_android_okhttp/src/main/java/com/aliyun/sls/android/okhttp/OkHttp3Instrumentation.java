package com.aliyun.sls.android.okhttp;

import com.aliyun.sls.android.ot.Span;
import com.aliyun.sls.android.ot.context.ContextManager;
import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;

import static com.aliyun.sls.android.okhttp.OKHttp3Tracer.OK_HTTP_3_TRACER_INTERCEPTOR;

/**
 * @author gordon
 * @date 2022/12/1
 */
public final class OkHttp3Instrumentation {

    private OkHttp3Instrumentation() {
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
        builder.addInterceptor(OK_HTTP_3_TRACER_INTERCEPTOR);
    }

    public static Call.Factory newCallFactory(OkHttpClient client) {
        return OKHttp3Tracer.newCallFactory(client);
    }
}
