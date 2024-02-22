package com.aliyun.sls.android.webview.instrumentation;

import android.annotation.SuppressLint;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import androidx.annotation.VisibleForTesting;
import androidx.webkit.WebViewCompat;
import androidx.webkit.WebViewFeature;
import com.aliyun.sls.android.webview.instrumentation.instrumentation.IWebRequestInstrumentation;
import com.aliyun.sls.android.webview.instrumentation.instrumentation.WebRequestInstrumentation;
import com.aliyun.sls.android.webview.instrumentation.jsbridge.OTelJSI;

/**
 * @author gordon
 * @date 2023/6/21
 */
public class WebViewInstrumentation {
    @VisibleForTesting(otherwise = VisibleForTesting.PACKAGE_PRIVATE)
    public final String userAgent;
    @VisibleForTesting(otherwise = VisibleForTesting.PACKAGE_PRIVATE)
    public final WebView webView;

    @VisibleForTesting(otherwise = VisibleForTesting.PACKAGE_PRIVATE)
    public final WebViewClient webViewClient;

    /* package */ WebViewInstrumentationConfiguration configuration;
    /* package */ IWebRequestInstrumentation requestInstrumentation;

    public WebViewInstrumentation(WebView webView) {
        this(webView, null);
    }

    @SuppressLint("SetJavaScriptEnabled")
    public WebViewInstrumentation(WebView webView, WebViewInstrumentationConfiguration configuration) {
        this.webView = webView;
        if (WebViewFeature.isFeatureSupported(WebViewFeature.GET_WEB_VIEW_CLIENT)) {
            this.webViewClient = WebViewCompat.getWebViewClient(this.webView);
        } else {
            // read WebViewClient from cache.
            // AGP plugin should hook setWebViewClient method.
            this.webViewClient = (WebViewClient)webView.getTag(R.id.aliyun_sls_webview_hook_id);
        }

        this.webView.setWebViewClient(new WebViewClient());

        this.configuration = configuration;
        this.requestInstrumentation = new WebRequestInstrumentation(this, this.configuration);

        WebSettings settings = webView.getSettings();
        this.userAgent = settings.getUserAgentString();

        settings.setJavaScriptEnabled(true);
        settings.setDatabaseEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setAllowUniversalAccessFromFileURLs(true);
        webView.addJavascriptInterface(new OTelJSI(this.configuration, this.requestInstrumentation), "otelJsi");
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

}
