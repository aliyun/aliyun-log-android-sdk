package com.aliyun.sls.android.webview.instrumentation;

import android.annotation.SuppressLint;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import androidx.annotation.VisibleForTesting;
import com.aliyun.sls.android.webview.instrumentation.PayloadManager.WebRequestInfo;
import com.aliyun.sls.android.webview.instrumentation.instrumentation.IWebRequestInstrumentation;
import com.aliyun.sls.android.webview.instrumentation.instrumentation.WebRequestInstrumentation;
import com.aliyun.sls.android.webview.instrumentation.jsbridge.OTelJSI;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Span;

/**
 * @author gordon
 * @date 2023/6/21
 */
public class WebViewInstrumentation {
    @VisibleForTesting(otherwise = VisibleForTesting.PACKAGE_PRIVATE)
    public final String userAgent;
    @VisibleForTesting(otherwise = VisibleForTesting.PACKAGE_PRIVATE)
    public final WebView webView;

    /* package */ WebViewInstrumentationConfiguration configuration;
    /* package */ IWebRequestInstrumentation requestInstrumentation;

    @SuppressLint("SetJavaScriptEnabled")
    public WebViewInstrumentation(WebView webView, WebViewInstrumentationConfiguration configuration) {
        this.webView = webView;
        this.configuration = configuration;
        this.requestInstrumentation = new WebRequestInstrumentation(this, this.configuration);

        WebSettings settings = webView.getSettings();
        this.userAgent = settings.getUserAgentString();

        settings.setJavaScriptEnabled(true);
        settings.setDatabaseEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setAllowUniversalAccessFromFileURLs(true);
        webView.addJavascriptInterface(new OTelJSI(this.requestInstrumentation), "otelJsi");
    }

    public IWebRequestInstrumentation getRequestInstrumentation() {
        return requestInstrumentation;
    }

    public void start() {
        this.webView.setWebViewClient(new TelemetryWebViewClient(this));
    }

    public String getUserAgent() {
        return this.userAgent;
    }

    public static class WebViewInstrumentationConfiguration {

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
}
