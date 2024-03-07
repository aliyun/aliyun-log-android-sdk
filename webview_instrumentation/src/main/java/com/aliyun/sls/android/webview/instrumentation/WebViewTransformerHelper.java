package com.aliyun.sls.android.webview.instrumentation;

import android.webkit.WebView;
import android.webkit.WebViewClient;

/**
 * WebView AGP transformer helper class. Used for hook loadUrl & setWebViewClient.
 *
 * @author yulong.gyl
 * @date 2023/8/15
 * @noinspection unused
 */
public class WebViewTransformerHelper {
    private WebViewTransformerHelper() {
        //no instance
    }

    /**
     * Hook WebView
     *
     * @param webView
     * @noinspection unused
     */
    public static void hookWebView(WebView webView) {
        WebViewInstrumentationConfiguration configuration = GlobalWebViewInstrumentation.getGlobalConfiguration();
        WebViewInstrumentation instrumentation = new WebViewInstrumentation(webView, configuration);
        instrumentation.start();
    }

    /**
     * Hook WebView.setWebViewClient method
     *
     * @param webView
     * @param client
     * @noinspection unused
     */
    public static void hookWebViewClient(WebView webView, WebViewClient client) {
        webView.setTag(R.id.aliyun_sls_webview_hook_id, client);
    }
}
