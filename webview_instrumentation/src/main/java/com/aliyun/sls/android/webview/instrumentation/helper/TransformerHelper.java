package com.aliyun.sls.android.webview.instrumentation.helper;

import android.webkit.WebView;
import com.aliyun.sls.android.webview.instrumentation.GlobalWebViewInstrumentation;
import com.aliyun.sls.android.webview.instrumentation.WebViewInstrumentation;
import com.aliyun.sls.android.webview.instrumentation.WebViewInstrumentationConfiguration;

/**
 * @author yulong.gyl
 * @date 2023/8/15
 */
public class TransformerHelper {
    private TransformerHelper() {
        //no instance
    }

    public static void init(WebView webView) {
        WebViewInstrumentationConfiguration configuration = GlobalWebViewInstrumentation.getGlobalConfiguration();
        WebViewInstrumentation instrumentation = new WebViewInstrumentation(webView, configuration);
        instrumentation.start();
    }
}
