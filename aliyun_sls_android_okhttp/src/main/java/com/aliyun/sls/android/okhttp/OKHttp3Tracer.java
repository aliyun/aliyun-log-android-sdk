package com.aliyun.sls.android.okhttp;

import com.aliyun.sls.android.ot.Span;
import com.aliyun.sls.android.ot.context.ContextManager;
import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;

/**
 * @author gordon
 * @date 2022/9/8
 */
public class OKHttp3Tracer {
    public OKHttp3Tracer(OkHttpClient.Builder builder) {
        OkHttp3Utils.registerTracerInterceptor(builder);
    }

    static final OKHttp3TracerInterceptor OK_HTTP_3_TRACER_INTERCEPTOR = new OKHttp3TracerInterceptor();

    public static Call.Factory newCallFactory(OkHttpClient client) {
        OkHttpClient httpClient = client.newBuilder().addInterceptor(OK_HTTP_3_TRACER_INTERCEPTOR).build();
        return new CallFactory(httpClient);
    }

    public static void registerOKHttp3InstrumentationDelegate(final OKHttp3InstrumentationDelegate delegate) {
        OK_HTTP_3_TRACER_INTERCEPTOR.registerOKHttp3InstrumentationDelegate(delegate);
    }

    private static class CallFactory implements Call.Factory {
        private final OkHttpClient client;

        CallFactory(OkHttpClient client) {
            this.client = client;
        }

        @Override
        public Call newCall(Request request) {
            Request.Builder builder = request.newBuilder();
            final Span span = ContextManager.INSTANCE.activeSpan();
            if (null != span) {
                builder.tag(Span.class, span);
            }
            final Request requestCopy = builder.build();
            return client.newCall(requestCopy);
        }
    }
}
