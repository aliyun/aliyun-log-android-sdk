package com.aliyun.sls.android.webview.instrumentation;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.json.JSONObject;

/**
 * @author yulong.gyl
 * @date 2023/6/25
 */
public class PayloadManager {

    private static final Map<String, WebRequestInfo> cachedRequests = new ConcurrentHashMap<>();

    public static boolean contains(String requestId) {
        return cachedRequests.containsKey(requestId);
    }

    public static WebRequestInfo get(String requestId) {
        return cachedRequests.get(requestId);
    }

    public static void set(String requestId, WebRequestInfo requestBean) {
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
