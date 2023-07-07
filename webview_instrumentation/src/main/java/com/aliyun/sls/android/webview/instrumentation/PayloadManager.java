package com.aliyun.sls.android.webview.instrumentation;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import androidx.annotation.VisibleForTesting;
import org.json.JSONObject;

/**
 * @author yulong.gyl
 * @date 2023/6/25
 */
public class PayloadManager {
    private PayloadManager() {
        //no instance
    }

    private static class Holder {
        final static PayloadManager INSTANCE = new PayloadManager();
    }

    public static PayloadManager getInstance() {
        return Holder.INSTANCE;
    }

    @VisibleForTesting
    public final Map<String, WebRequestInfo> cachedRequests = new ConcurrentHashMap<>();

    public WebRequestInfo get(String requestId) {
        return cachedRequests.get(requestId);
    }

    public void remove(String requestId) {
        if (cachedRequests.containsKey(requestId)) {
            cachedRequests.remove(requestId);
        }
    }

    public void set(String requestId, WebRequestInfo requestBean) {
        cachedRequests.put(requestId, requestBean);
    }

    public static class WebRequestInfo {
        public String requestId;
        public String url;
        public String method;
        public String origin;
        public JSONObject headers;
        public String mimeType;
        public String body;

        public int responseStatus;
        public String responseStatusText;
        public JSONObject responseHeaders;
        public String responseBody;

        @Override
        public String toString() {
            return "WebRequestInfo{" +
                "requestId='" + requestId + '\'' +
                ", url='" + url + '\'' +
                ", method='" + method + '\'' +
                ", origin='" + origin + '\'' +
                ", headers=" + headers +
                ", mimeType='" + mimeType + '\'' +
                ", body='" + body + '\'' +
                ", responseStatus='" + responseStatus + '\'' +
                ", responseStatusText='" + responseStatusText + '\'' +
                ", responseHeaders='" + responseHeaders + '\'' +
                ", responseBody='" + responseBody + '\'' +
                '}';
        }
    }
}
