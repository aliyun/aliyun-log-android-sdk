package com.aliyun.sls.android.webview.instrumentation.helper;

import android.webkit.WebView;
import com.aliyun.sls.android.webview.instrumentation.WebViewInstrumentation;
import com.aliyun.sls.android.webview.instrumentation.WebViewInstrumentation.WebViewInstrumentationConfiguration;
import io.opentelemetry.api.GlobalOpenTelemetry;

/**
 * @author yulong.gyl
 * @date 2023/8/15
 */
public class TransformerHelper {
    private TransformerHelper() {
        //no instance
    }

    public static void init(WebView webView) {
        WebViewInstrumentationConfiguration configuration = new WebViewInstrumentationConfiguration(
            GlobalOpenTelemetry.get());
        WebViewInstrumentation instrumentation = new WebViewInstrumentation(webView, configuration);
        instrumentation.start();
    }
}
