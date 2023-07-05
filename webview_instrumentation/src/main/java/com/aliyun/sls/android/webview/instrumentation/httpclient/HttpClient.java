package com.aliyun.sls.android.webview.instrumentation.httpclient;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import android.text.TextUtils;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import com.aliyun.sls.android.webview.instrumentation.WebViewInstrumentation;
import com.aliyun.sls.android.webview.instrumentation.utils.Utils;

/**
 * @author yulong.gyl
 * @date 2023/6/25
 */
public class HttpClient {
    private static final int DEFAULT_TIMEOUT_MILLISECONDS = 60 * 1000;

    private HttpClient() {
        //no instance
    }

    public static String requestHtml(WebViewInstrumentation instrumentation, WebView webView, WebResourceRequest request) {
        if (null == request) {
            return null;
        }

        try {
            URL url = new URL(request.getUrl().toString());
            String httpUrl = request.getUrl().toString() + (TextUtils.isEmpty(url.getQuery()) ? "?" : "&") + "otel_flag=web";
            url = new URL(httpUrl);

            HttpURLConnection connection = (HttpURLConnection)url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("User-Agent", instrumentation.getUserAgent());
            connection.setReadTimeout(DEFAULT_TIMEOUT_MILLISECONDS);
            connection.setConnectTimeout(DEFAULT_TIMEOUT_MILLISECONDS);

            final int retCode = connection.getResponseCode();

            // success
            if (retCode / 100 == 2) {
                InputStream ins = connection.getInputStream();
                return Utils.input2String(ins, "utf-8");
            }

            return null;

        } catch (Throwable t) {
            return null;
        }
    }

}
