package com.aliyun.sls.android.webview.instrumentation;

import android.webkit.WebResourceRequest;
import com.aliyun.sls.android.webview.instrumentation.PayloadManager.WebRequestInfo;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Span;

public class WebViewInstrumentationConfiguration {

    public final OpenTelemetry telemetry;

    public WebViewInstrumentationConfiguration(OpenTelemetry telemetry) {
        this.telemetry = telemetry;
    }

    public boolean shouldInstrument(WebResourceRequest request) {
        return true;
    }

    public boolean shouldRecordPayload(WebRequestInfo requestInfo) {
        return true;
    }

    public boolean shouldInjectTracingRequestHeaders(WebRequestInfo requestInfo) {
        return true;
    }

    public String nameSpan(WebRequestInfo requestInfo) {
        return null;
    }

    public void createdRequest(WebRequestInfo requestInfo, Span span) {

    }

    public boolean shouldInjectTracingResponseHeaders(WebRequestInfo requestInfo) {
        return true;
    }

    public boolean shouldInjectTracingResponseBody(WebRequestInfo requestInfo) {
        return true;
    }

    public void receivedResponse(WebRequestInfo requestInfo, Span span) {

    }

    public boolean debuggable() {
        return true;
    }

}