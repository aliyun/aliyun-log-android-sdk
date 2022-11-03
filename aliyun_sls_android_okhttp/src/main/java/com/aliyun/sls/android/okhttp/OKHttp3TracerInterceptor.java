package com.aliyun.sls.android.okhttp;

import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;

import android.text.TextUtils;
import com.aliyun.sls.android.ot.Attribute;
import com.aliyun.sls.android.ot.Span;
import com.aliyun.sls.android.ot.Span.StatusCode;
import com.aliyun.sls.android.ot.SpanBuilder;
import com.aliyun.sls.android.ot.context.ContextManager;
import com.aliyun.sls.android.ot.context.Scope;
import com.aliyun.sls.android.trace.Tracer;
import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Request.Builder;
import okhttp3.Response;

/**
 * @author gordon
 * @date 2022/9/8
 */
public class OKHttp3TracerInterceptor implements Interceptor {
    private OKHttp3InstrumentationDelegate delegate;

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        if (null != delegate && !delegate.shouldInstrument(request)) {
            return chain.proceed(request);
        }

        Span parent = (Span) request.tag(Span.class);
        SpanBuilder builder = Tracer.spanBuilder("HTTP " + request.method());
        if (null != parent) {
            builder.setParent(parent);
        }

        HttpUrl url = request.url();
        builder.addAttribute(Attribute.of("http.method", request.method()));
        builder.addAttribute(Attribute.of("http.url", url.toString()));
        builder.addAttribute(Attribute.of("http.target", url.encodedPath()));
        builder.addAttribute(Attribute.of("net.peer.name", url.host()));
        builder.addAttribute(Attribute.of("http.scheme", url.scheme()));
        builder.addAttribute(Attribute.of("net.peer.port", String.valueOf(url.port())));

        Span span = builder.build();

        Response response;
        try (Scope ignored = ContextManager.INSTANCE.makeCurrent(span)) {
            final String traceId = span.getTraceId();
            final String spanId = span.getSpanId();

            final String traceparent = String.format("00-%s-%s-01", traceId, spanId);
            Builder requestBuilder = request.newBuilder();
            requestBuilder.header("traceparent", traceparent);
            injectCustomHeaders(request, requestBuilder);

            response = chain.proceed(requestBuilder.build());
        } catch (Throwable e) {
            span.setStatus(StatusCode.ERROR);
            span.recordException(e);
            throw e;
        } finally {
            span.end();
        }

        return response;
    }

    private void injectCustomHeaders(Request request, Builder builder) {
        if (null == delegate) {
            return;
        }

        Map<String, String> customHeaders = delegate.injectCustomHeaders(request);
        if (null == customHeaders || customHeaders.size() == 0) {
            return;
        }

        for (Entry<String, String> entry : customHeaders.entrySet()) {
            if (TextUtils.isEmpty(entry.getKey()) || TextUtils.isEmpty(entry.getValue())) {
                continue;
            }

            builder.header(entry.getKey(), entry.getValue());
        }
    }

    public void registerOKHttp3InstrumentationDelegate(OKHttp3InstrumentationDelegate delegate) {
        this.delegate = delegate;
    }
}
