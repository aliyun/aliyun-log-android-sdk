package com.aliyun.sls.android.okhttp;

import java.util.LinkedHashMap;
import java.util.Map;

import com.aliyun.sls.android.ot.Span;
import com.aliyun.sls.android.ot.context.ContextManager;
import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;

/**
 * @author gordon
 * @date 2022/9/8
 */
public class OkHttpTelemetry {

    private static final Map<Request, Span> SPAN_CACHE = new LinkedHashMap<>();

    public static Call.Factory newCallFactory(OkHttpClient client) {
        OkHttpClient httpClient = client.newBuilder().addInterceptor(new OkHttpTracingInterceptor()).build();
        return new CallFactory(httpClient);
    }

    static Span getSpanByRequest(Request request) {
        if (SPAN_CACHE.containsKey(request)) {
            return SPAN_CACHE.get(request);
        }

        return null;
    }

    static void setSpanByRequest(Request request, Span span) {
        SPAN_CACHE.put(request, span);
    }

    private static class CallFactory implements Call.Factory {
        private final OkHttpClient client;

        CallFactory(OkHttpClient client) {
            this.client = client;
        }

        @Override
        public Call newCall(Request request) {
            final Span span = ContextManager.INSTANCE.activeSpan();
            if (null != span) {
                setSpanByRequest(request, span);
            }

            return client.newCall(request);
        }
    }
}
