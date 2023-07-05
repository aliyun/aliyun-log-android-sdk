package com.aliyun.sls.android.webview.instrumentation;

import android.annotation.SuppressLint;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import com.aliyun.sls.android.webview.instrumentation.instrumentation.IWebRequestInstrumentation;
import com.aliyun.sls.android.webview.instrumentation.instrumentation.WebRequestInstrumentation;
import com.aliyun.sls.android.webview.instrumentation.jsbridge.OTelJSI;
import io.opentelemetry.api.OpenTelemetry;

/**
 * @author gordon
 * @date 2023/6/21
 */
public class WebViewInstrumentation {
    private String userAgent;
    private WebView webView;

    /* package */ WebViewInstrumentationConfiguration configuration;
    /* package */ IWebRequestInstrumentation requestInstrumentation;

    @SuppressLint("SetJavaScriptEnabled")
    public WebViewInstrumentation(WebView webView, WebViewInstrumentationConfiguration configuration) {
        this.webView = webView;
        this.configuration = configuration;
        this.requestInstrumentation = new WebRequestInstrumentation(this.configuration.telemetry);

        WebSettings settings = webView.getSettings();
        this.userAgent = settings.getUserAgentString();

        webView.setWebContentsDebuggingEnabled(true);
        settings.setJavaScriptEnabled(true);
        settings.setDatabaseEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setAllowUniversalAccessFromFileURLs(true);
        webView.addJavascriptInterface(new OTelJSI(this.requestInstrumentation), "otelJsi");
        //this.webView.getSettings().setJavaScriptEnabled(true);
    }

    public void start() {
        this.webView.setWebViewClient(new TelemetryWebViewClient(this));
    }

    public String getUserAgent() {
        return this.userAgent;
    }

    public static class WebViewInstrumentationConfiguration {

        OpenTelemetry telemetry;

        public WebViewInstrumentationConfiguration(OpenTelemetry telemetry) {
            this.telemetry = telemetry;
        }

        protected boolean shouldInstrument(WebResourceRequest request) {
            return true;
        }

    }
}
