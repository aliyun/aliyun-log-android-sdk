package com.aliyun.sls.android.webview.instrumentation.instrumentation;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.aliyun.sls.android.webview.instrumentation.PayloadManager.WebRequestInfo;
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

    private final Map<String, Span> cachedSpan = new ConcurrentHashMap<>();
    private final Tracer tracer;

    public WebRequestInstrumentation(OpenTelemetry telemetry) {
        this.tracer = telemetry.getTracer("WebView-Instrumentation", "1.0.0");
    }

    @Override
    public void requestStarted(WebRequestInfo info) {
        Span span = tracer.spanBuilder("HTTP " + info.method + "").startSpan();
        span.setAttribute(SemanticAttributes.HTTP_URL, info.url);
        span.setAttribute(SemanticAttributes.HTTP_METHOD, info.method);
        span.setAttribute(SemanticAttributes.HTTP_HOST, info.origin);

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
