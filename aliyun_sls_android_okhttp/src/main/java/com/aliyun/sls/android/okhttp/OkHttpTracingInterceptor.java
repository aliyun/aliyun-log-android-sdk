package com.aliyun.sls.android.okhttp;

import java.io.IOException;

import com.aliyun.sls.android.ot.Span;
import com.aliyun.sls.android.ot.SpanBuilder;
import com.aliyun.sls.android.ot.context.ContextManager;
import com.aliyun.sls.android.ot.context.Scope;
import com.aliyun.sls.android.trace.Tracer;
import okhttp3.Interceptor;
import okhttp3.Response;

/**
 * @author gordon
 * @date 2022/9/8
 */
public class OkHttpTracingInterceptor implements Interceptor {
    @Override
    public Response intercept(Chain chain) throws IOException {
        Span parent = OkHttpTelemetry.getSpanByRequest(chain.request());
        SpanBuilder builder = Tracer.spanBuilder("HTTP request");
        if (null != parent) {
            builder.setParent(parent);
        }
        Span span = builder.build();

        Response response;
        try (Scope ignored = ContextManager.INSTANCE.makeCurrent(span)) {
            final String traceId = span.traceID;
            final String spanId = span.spanID;

            final String traceparent = String.format("00-%s-%s-01", traceId, spanId);
            response = chain.proceed(chain.request().newBuilder().header("traceparent", traceparent).build());
        } finally {
            span.end();
        }

        return response;
    }
}
