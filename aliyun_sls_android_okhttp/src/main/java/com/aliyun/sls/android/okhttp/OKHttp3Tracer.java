package com.aliyun.sls.android.okhttp;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;

/**
 * @author gordon
 * @date 2022/9/8
 */
public class OKHttp3Tracer {
    public OKHttp3Tracer(OkHttpClient.Builder builder) {
        OkHttp3Instrumentation.registerTracerInterceptor(builder);
    }

    static final OKHttp3TracerInterceptor OK_HTTP_3_TRACER_INTERCEPTOR = new OKHttp3TracerInterceptor();

    public static Call.Factory newCallFactory(OkHttpClient client) {
        OkHttpClient httpClient = client.newBuilder().addInterceptor(OK_HTTP_3_TRACER_INTERCEPTOR).build();
        return new CallFactory(httpClient);
    }

    public static void registerOKHttp3InstrumentationDelegate(final OKHttp3InstrumentationDelegate delegate) {
        OK_HTTP_3_TRACER_INTERCEPTOR.registerOKHttp3InstrumentationDelegate(delegate);
    }

    public static void updateOkHttp3Configuration(OkHttp3Configuration configuration) {
        OK_HTTP_3_TRACER_INTERCEPTOR.updateConfiguration(configuration);
    }

    private static class CallFactory implements Call.Factory {
        private final OkHttpClient client;

        CallFactory(OkHttpClient client) {
            this.client = client;
        }

        @Override
        public Call newCall(Request request) {
            final Request requestCopy = OkHttp3Instrumentation.newRequest(request);
            return client.newCall(requestCopy);
        }
    }
}
