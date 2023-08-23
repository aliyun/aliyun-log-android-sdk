package com.aliyun.sls.android.webview.instrumentation.httpclient;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import android.text.TextUtils;
import android.webkit.WebResourceRequest;
import com.aliyun.sls.android.webview.instrumentation.PayloadManager.WebRequestInfo;
import com.aliyun.sls.android.webview.instrumentation.WebViewInstrumentation;
import com.aliyun.sls.android.webview.instrumentation.instrumentation.IWebRequestInstrumentation;
import com.aliyun.sls.android.webview.instrumentation.utils.Utils;
import org.json.JSONObject;

/**
 * @author yulong.gyl
 * @date 2023/6/25
 */
public class HttpClient {
    private static final int DEFAULT_TIMEOUT_MILLISECONDS = 60 * 1000;

    private HttpClient() {
        //no instance
    }

    public static String requestHtml(WebViewInstrumentation instrumentation, WebResourceRequest request) {
        if (null == request) {
            return null;
        }

        final String requestId = new Date().getTime() + Math.floor(Math.random() * 100000) + "";
        try {
            URL url = new URL(request.getUrl().toString());
            String httpUrl = request.getUrl().toString() +
                (TextUtils.isEmpty(url.getQuery()) ? "?" : "&") +
                "otel_flag=web";
            url = new URL(httpUrl);

            HttpURLConnection connection = (HttpURLConnection)url.openConnection();
            connection.setRequestMethod("GET");

            Map<String, String> headers = new HashMap<String, String>();
            if (null != request.getRequestHeaders() && request.getRequestHeaders().size() > 0) {
                headers.putAll(request.getRequestHeaders());
            }
            headers.put("User-Agent", instrumentation.getUserAgent());

            for (Entry<String, String> entry : headers.entrySet()) {
                connection.setRequestProperty(entry.getKey(), entry.getValue());
            }

            connection.setReadTimeout(DEFAULT_TIMEOUT_MILLISECONDS);
            connection.setConnectTimeout(DEFAULT_TIMEOUT_MILLISECONDS);

            traceCreatedHttpRequest(instrumentation.getRequestInstrumentation(), request, headers, requestId);

            final int retCode = connection.getResponseCode();
            // success
            if (retCode / 100 == 2) {
                final String body = readInputStream(connection);
                traceReceivedHttpResponse(instrumentation.getRequestInstrumentation(), retCode, null, body, requestId);
                return body;
            }

            final String error = readInputStream(connection);
            traceReceivedHttpResponse(instrumentation.getRequestInstrumentation(), retCode, error, null, requestId);
            return error;

        } catch (Throwable t) {
            traceReceivedHttpResponse(instrumentation.getRequestInstrumentation(), 400, t.getMessage(), null,
                requestId);
            return null;
        }
    }

    private static String readInputStream(HttpURLConnection connection) {
        try {
            return Utils.input2String(connection.getInputStream(), "utf-8");
        } catch (Throwable e) {
            try {
                return Utils.input2String(connection.getErrorStream(), "utf-8");
            } catch (Throwable e1) {
                return "";
            }
        }
    }

    private static void traceCreatedHttpRequest(IWebRequestInstrumentation instrumentation,
        WebResourceRequest request, Map<String, String> headers, String requestId) {

        WebRequestInfo info = new WebRequestInfo();
        info.requestId = requestId;
        info.url = request.getUrl().toString();
        info.method = "GET";
        info.origin = info.url;
        info.mimeType = "text/html";
        info.headers = new JSONObject(headers);

        instrumentation.createdRequest(info);
    }

    private static void traceReceivedHttpResponse(IWebRequestInstrumentation instrumentation, int code,
        String errorStatus, String body, String requestId) {

        WebRequestInfo info = new WebRequestInfo();
        info.requestId = requestId;
        info.responseStatus = code;
        info.responseStatusText = errorStatus;
        info.responseBody = body;

        instrumentation.receivedResponse(info);
    }
}
