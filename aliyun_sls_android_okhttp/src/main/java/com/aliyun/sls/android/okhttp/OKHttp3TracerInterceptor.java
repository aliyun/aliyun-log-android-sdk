package com.aliyun.sls.android.okhttp;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import android.text.TextUtils;
import android.util.Pair;
import com.aliyun.sls.android.ot.Attribute;
import com.aliyun.sls.android.ot.Span;
import com.aliyun.sls.android.ot.Span.StatusCode;
import com.aliyun.sls.android.ot.SpanBuilder;
import com.aliyun.sls.android.ot.context.ContextManager;
import com.aliyun.sls.android.ot.context.Scope;
import com.aliyun.sls.android.ot.utils.JSONUtils;
import com.aliyun.sls.android.trace.Tracer;
import okhttp3.Headers;
import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Request.Builder;
import okhttp3.RequestBody;
import okhttp3.Response;
import okio.Buffer;
import org.json.JSONObject;

/**
 * @author gordon
 * @date 2022/9/8
 */
public class OKHttp3TracerInterceptor implements Interceptor {
    /* 50 KB*/
    private static final long KB_50 = 50 * 1024;

    private OKHttp3InstrumentationDelegate delegate;
    private OkHttp3Configuration configuration = new OkHttp3Configuration();

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        if (null != delegate && !delegate.shouldInstrument(request)) {
            return chain.proceed(request);
        }

        Span parent = request.tag(Span.class);
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

        Attribute attribute = captureHeaders(request);
        if (null != attribute) {
            builder.addAttribute(attribute);
        }

        attribute = captureBody(request);
        if (null != attribute) {
            builder.addAttribute(attribute);
        }

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
            List<Attribute> responseAttribute = captureResponse(response);
            if (null != responseAttribute) {
                span.addAttribute(responseAttribute);
            }
        } catch (Throwable e) {
            span.setStatus(StatusCode.ERROR);
            span.recordException(e);
            throw e;
        } finally {
            span.end();
        }

        return response;
    }

    private Attribute captureHeaders(Request request) {
        if (null == configuration || !configuration.captureHeaders) {
            return null;
        }

        if (null == request.headers()) {
            return null;
        }

        String headerString = headers2string(request.headers());
        if (TextUtils.isEmpty(headerString)) {
            return null;
        }

        return Attribute.of("http.headers", headerString);
    }

    private String headers2string(Headers header) {
        Map<String, List<String>> headers = header.toMultimap();
        if (headers.isEmpty()) {
            return null;
        }

        JSONObject headerObject = new JSONObject();
        for (Entry<String, List<String>> entry : headers.entrySet()) {
            String name = entry.getKey();
            List<String> values = entry.getValue();
            if (TextUtils.isEmpty(name) || null == values) {
                continue;
            }

            StringBuilder valuesBuilder = new StringBuilder();
            for (String s : values) {
                valuesBuilder.append(s);
                valuesBuilder.append(",");
            }

            JSONUtils.put(headerObject, name, valuesBuilder.substring(0, valuesBuilder.length() - 1));
        }

        return headerObject.toString();
    }

    private Attribute captureBody(Request request) {
        if (null == configuration || !configuration.captureBody) {
            return null;
        }

        if (null == request.body()) {
            return null;
        }

        String bodyString = body2String(request.body());
        if (TextUtils.isEmpty(bodyString)) {
            return null;
        }
        return Attribute.of("http.body", bodyString);
    }

    private String body2String(RequestBody body) {
        try {
            Buffer buffer = new Buffer();
            body.writeTo(buffer);
            return buffer.readUtf8();
        } catch (IOException e) {
            return null;
        }
    }

    private List<Attribute> captureResponse(Response response) {
        if (null == response || !configuration.captureResponse) {
            return null;
        }

        if (null == response.body()) {
            return null;
        }

        final String code = String.valueOf(response.code());
        final String headers = headers2string(response.headers());
        final String message = response.message();
        String body = null;
        try {
            final long maxLength = Math.min(KB_50, response.body().contentLength());
            if (maxLength >= 0) {
                body = response.peekBody(maxLength).string();
            }
        } catch (IOException e) {
            // ignore
        }

        return Attribute.of(
            Pair.create("http.response.code", code),
            Pair.create("http.response.headers", TextUtils.isEmpty(headers) ? "" : headers),
            Pair.create("http.response.message", TextUtils.isEmpty(message) ? "" : message),
            Pair.create("http.response.body", TextUtils.isEmpty(body) ? "" : body)
        );
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

    public void updateConfiguration(OkHttp3Configuration configuration) {
        this.configuration = configuration;
    }
}
