package com.aliyun.sls.android.webview.instrumentation.instrumentation;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import android.net.Uri;
import android.text.TextUtils;
import com.aliyun.sls.android.webview.instrumentation.PayloadManager.WebRequestInfo;
import com.aliyun.sls.android.webview.instrumentation.WebViewInstrumentation;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.semconv.trace.attributes.SemanticAttributes;

/**
 * @author yulong.gyl
 * @date 2023/7/4
 */
public class WebRequestInstrumentation implements IWebRequestInstrumentation {

    private final WebViewInstrumentation instrumentation;
    private final Tracer tracer;
    private final Map<String, Span> cachedSpan = new ConcurrentHashMap<>();


    public WebRequestInstrumentation(WebViewInstrumentation instrumentation, OpenTelemetry telemetry) {
        this.instrumentation = instrumentation;
        this.tracer = telemetry.getTracer("WebView-Instrumentation", "1.0.0");
    }

    @Override
    public void requestStarted(WebRequestInfo info) {
        if (TextUtils.isEmpty(info.url)) {
            return;
        }

        Uri uri = Uri.parse(info.url);
        Span span = tracer.spanBuilder(String.format("Web %s %s", info.method, uri.getPath())).startSpan();

        span.setAttribute(SemanticAttributes.HTTP_URL, info.url);
        span.setAttribute(SemanticAttributes.HTTP_METHOD, info.method);
        span.setAttribute("http.path", uri.getPath());
        span.setAttribute("http.origin", info.origin);
        span.setAttribute(SemanticAttributes.HTTP_USER_AGENT, instrumentation.getUserAgent());

        cachedSpan.put(info.requestId, span);
    }

    @Override
    public void requestHeadersUpdated(WebRequestInfo info) {
        final Span span = cachedSpan.get(info.requestId);
        if (null == span) {
            return;
        }

        span.setAttribute("http.headers", info.headers.toString());
    }

    @Override
    public void requestMimeTypeUpdated(WebRequestInfo info) {
        final Span span = cachedSpan.get(info.requestId);
        if (null == span) {
            return;
        }

        span.setAttribute("http.mimeType", info.mimeType);
    }

    @Override
    public void requestBodyUpdated(WebRequestInfo info) {
        final Span span = cachedSpan.get(info.requestId);
        if (null == span) {
            return;
        }

        span.setAttribute("http.body", info.body);
    }

    @Override
    public void responseReturned(WebRequestInfo info) {
        final Span span = cachedSpan.get(info.requestId);
        if (null == span) {
            return;
        }

        span.setAttribute(SemanticAttributes.HTTP_STATUS_CODE, info.responseStatus);
        span.setAttribute(SemanticAttributes.OTEL_STATUS_DESCRIPTION, info.responseStatusText);
        span.setAttribute("http.response.headers", info.responseHeaders.toString());
        span.setAttribute("http.response.body", info.responseBody);

        if (info.responseStatus / 100 != 2) {
            span.setStatus(StatusCode.ERROR, info.responseStatusText);
        }

        span.end();
    }
}
