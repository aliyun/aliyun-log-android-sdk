package com.aliyun.sls.android.webview.instrumentation.jsbridge;

import android.util.Log;
import android.webkit.JavascriptInterface;
import com.aliyun.sls.android.webview.instrumentation.PayloadManager;
import com.aliyun.sls.android.webview.instrumentation.PayloadManager.WebRequestInfo;
import com.aliyun.sls.android.webview.instrumentation.instrumentation.IWebRequestInstrumentation;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author yulong.gyl
 * @date 2023/6/25
 */
public class OTelJSI {
    private static final String TAG = "DoKitJSI";

    private final IWebRequestInstrumentation requestInstrumentation;
    public OTelJSI(IWebRequestInstrumentation requestInstrumentation) {
        this.requestInstrumentation = requestInstrumentation;
    }

    @JavascriptInterface
    public void fetch(String requestId, String url, String method, String origin, String headers, String body) {
        Log.d(TAG,
            "fetch. requestId: " + requestId + ", method: " + method + ", headers: " + headers + ", body: " + body
                + ", url: " + url + ", origin: " + origin);
        WebRequestInfo webRequestInfo = PayloadManager.get(requestId);
        if (null == webRequestInfo) {
            webRequestInfo = new WebRequestInfo();
            PayloadManager.set(requestId, webRequestInfo);
        }

        webRequestInfo.requestId = requestId;
        webRequestInfo.url = url.startsWith("http") ? url : origin + (url.startsWith("/") ? url : ("/" + url));
        webRequestInfo.method = method;
        webRequestInfo.origin = origin;
        webRequestInfo.mimeType = null;
        webRequestInfo.body = body;

        webRequestInfo.headers = header2JSON(headers);

        requestInstrumentation.requestStarted(webRequestInfo);
    }

    private JSONObject header2JSON(String headers) {
        if (null == headers) {
            return new JSONObject();
        }

        try {
            return new JSONObject(headers);
        } catch (JSONException e) {
            e.printStackTrace();
            return new JSONObject();
        }
    }

    @JavascriptInterface
    public void open(String requestId, String url, String method, String origin) {
        Log.d(TAG, "open. requestId: " + requestId + ", method: " + method + ", url: " + url + ", origin: " + origin);
        WebRequestInfo webRequestInfo = PayloadManager.get(requestId);
        if (null == webRequestInfo) {
            webRequestInfo = new WebRequestInfo();
            PayloadManager.set(requestId, webRequestInfo);
        }

        webRequestInfo.requestId = requestId;
        webRequestInfo.url = url.startsWith("http") ? url : origin + (url.startsWith("/") ? url : ("/" + url));
        webRequestInfo.method = method;
        webRequestInfo.origin = origin;

        requestInstrumentation.requestStarted(webRequestInfo);
    }

    @JavascriptInterface
    public void setRequestHeader(String requestId, String key, String value) {
        Log.d(TAG, "setRequestHeader. requestId: " + requestId + ", key: " + key + ", value: " + value);
        WebRequestInfo webRequestInfo = PayloadManager.get(requestId);
        if (null == webRequestInfo) {
            webRequestInfo = new WebRequestInfo();
            PayloadManager.set(requestId, webRequestInfo);
        }

        if (null == webRequestInfo.headers) {
            webRequestInfo.headers = new JSONObject();
        }

        putHeader(webRequestInfo.headers, key, value);

        requestInstrumentation.requestHeadersUpdated(webRequestInfo);
    }

    private void putHeader(JSONObject headers, String key, String value) {
        try {
            headers.put(key, value);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @JavascriptInterface
    public void overrideMimeType(String requestId, String mimeType) {
        Log.d(TAG, "overrideMimeType. requestId: " + requestId + ", mimeType: " + mimeType);
        WebRequestInfo webRequestInfo = PayloadManager.get(requestId);
        if (null != webRequestInfo) {
            webRequestInfo.mimeType = mimeType;
            requestInstrumentation.requestMimeTypeUpdated(webRequestInfo);
        }
    }

    @JavascriptInterface
    public void send(String requestId, String body) {
        Log.d(TAG, "send. requestId: " + requestId + ", body: " + body);
        WebRequestInfo requestInfo = PayloadManager.get(requestId);
        if (null != requestInfo) {
            requestInfo.body = body;

            requestInstrumentation.requestBodyUpdated(requestInfo);
        }
    }

    @JavascriptInterface
    public void handleResponse(String requestId, int status, String statusText, String text, String headers) {
        Log.d(TAG, "handleResponse. requestId: " + requestId + ", status: " + status + ", statusText: " + statusText + ", headers: " + headers + ", response: " + text.substring(0, Math.min(128, text.length())));

        WebRequestInfo info = PayloadManager.get(requestId);
        if (null != info) {
            info.responseStatus = status;
            info.responseStatusText = statusText;
            info.responseHeaders = header2JSON(headers);
            info.responseBody = text;

            requestInstrumentation.responseReturned(info);
        } else {
            Log.w(TAG, "handleResponse. not found WebRequestInfo");
        }
    }
}
