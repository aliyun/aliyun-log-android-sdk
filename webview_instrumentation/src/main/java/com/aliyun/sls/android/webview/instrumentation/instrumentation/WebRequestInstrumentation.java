package com.aliyun.sls.android.webview.instrumentation.instrumentation;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import android.net.Uri;
import android.text.TextUtils;
import com.aliyun.sls.android.webview.instrumentation.PayloadManager.WebRequestInfo;
import com.aliyun.sls.android.webview.instrumentation.WebViewInstrumentation;
import com.aliyun.sls.android.webview.instrumentation.WebViewInstrumentation.WebViewInstrumentationConfiguration;
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
    private final WebViewInstrumentationConfiguration configuration;
    private final Tracer tracer;
    private final Map<String, Span> cachedSpan = new ConcurrentHashMap<>();

    public WebRequestInstrumentation(
        WebViewInstrumentation instrumentation,
        WebViewInstrumentationConfiguration configuration) {

        this.instrumentation = instrumentation;
        this.configuration = configuration;
        this.tracer = this.configuration.telemetry.getTracer("WebView-Instrumentation", "1.0.0");
    }

    @Override
    public void createdRequest(WebRequestInfo info) {
        if (TextUtils.isEmpty(info.url)) {
            return;
        }

        Uri uri = Uri.parse(info.url);
        String spanName = configuration.nameSpan(info);
        Span span = tracer.spanBuilder(
                TextUtils.isEmpty(spanName) ? String.format("Web %s %s", info.method, uri.getPath()) : spanName)
            .startSpan();

        span.setAttribute(SemanticAttributes.HTTP_URL, info.url);
        span.setAttribute(SemanticAttributes.HTTP_METHOD, info.method);
        span.setAttribute("http.path", uri.getPath());
        span.setAttribute("http.origin", info.origin);
        span.setAttribute("http.mimeType", info.mimeType);
        span.setAttribute(SemanticAttributes.HTTP_USER_AGENT, instrumentation.getUserAgent());

        if (configuration.shouldRecordPayload(info)) {
            span.setAttribute("http.body", info.body);
        }

        if (configuration.shouldInjectTracingRequestHeaders(info)) {
            span.setAttribute("http.headers", info.headers.toString());
        }

        configuration.createdRequest(info, span);

        cachedSpan.put(info.requestId, span);
    }

    @Override
    public void receivedResponse(WebRequestInfo info) {
        final Span span = cachedSpan.get(info.requestId);
        if (null == span) {
            return;
        }

        span.setAttribute(SemanticAttributes.HTTP_STATUS_CODE, info.responseStatus);
        span.setAttribute("http.status_description", info.responseStatusText);

        if (configuration.shouldInjectTracingResponseHeaders(info)) {
            span.setAttribute("http.response.headers", info.responseHeaders.toString());
        }

        if (configuration.shouldInjectTracingResponseBody(info)) {
            span.setAttribute("http.response.body", info.responseBody);
        }

        if (info.responseStatus / 100 != 2) {
            span.setStatus(StatusCode.ERROR, info.responseStatusText);
        }

        configuration.receivedResponse(info, span);

        span.end();
    }
}
