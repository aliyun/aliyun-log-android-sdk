package com.aliyun.sls.android.network_diagnosis;

import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.SSLContext;
import javax.net.ssl.X509TrustManager;

/**
 * @author gordon
 * @date 2022/7/22
 */
public interface INetworkDiagnosis {

    int DEFAULT_PING_SIZE = 64;
    int DEFAULT_TIMEOUT = 2 * 1000;
    int DEFAULT_MAX_TIMES = 10;

    int DEFAULT_MTR_MAX_TTL = 30;
    int DEFAULT_MTR_MAX_PATH = 1;

    int INVALID = -1;

    String DNS_TYPE_IPv4 = "A";
    String DNS_TYPE_IPv6 = "AAAA";

    enum Type {
        HTTP("http"),
        PING("ping"),
        TCPPING("tcpping"),
        MTR("mtr"),
        DNS("dns"),
        UNKNOWN("unknown");

        private static final Map<String, Type> ENUM_MAP = new HashMap<String, Type>(5) {
            {
                put(HTTP.value, HTTP);
                put(PING.value, PING);
                put(TCPPING.value, TCPPING);
                put(MTR.value, MTR);
                put(DNS.value, DNS);
            }
        };

        public final String value;

        Type(String value) {
            this.value = value;
        }

        public static Type of(String value) {
            return ENUM_MAP.containsKey(value.toLowerCase()) ? ENUM_MAP.get(value) : UNKNOWN;
        }

    }

    interface Callback {
        void onComplete(Type type, String ret);
    }

    class Response {
        public Type type;
        public String content;
        public Object context;
        public String error;

        public static Response response(Object context, Type type, String content) {
            Response response = new Response();
            response.context = context;
            response.type = type;
            response.content = content;
            return response;
        }

        public static Response error(String error) {
            Response response = new Response();
            response.error = error;

            return response;
        }
    }

    interface Callback2 {
        void onComplete(Response response);
    }

    class HttpCredential {
        private SSLContext sslContext;
        private X509TrustManager trustManager;

        public HttpCredential(SSLContext sslContext, X509TrustManager trustManager) {
            this.sslContext = sslContext;
            this.trustManager = trustManager;
        }

        public SSLContext getSslContext() {
            return this.sslContext;
        }

        public X509TrustManager getTrustManager() {
            return this.trustManager;
        }
    }

    interface HttpCredentialCallback {
        HttpCredential getCredential(String url, Object context);
    }

    // region setup
    void setPolicyDomain(String domain);

    void disableExNetworkInfo();

    /**
     * Enable the multiple ports detect. Default is false, use the default port.
     *
     * @param enable true/false
     */
    void setMultiplePortsDetect(boolean enable);

    void registerCallback(Callback callback);

    void updateExtensions(Map<String, String> extension);

    void registerHttpCredentialCallback(HttpCredentialCallback callback);
    // endregion

    // region http

    class Request {
        public String domain;
        public Object context;
    }

    class HttpRequest extends Request {
        public String ip;
        public HttpCredential credential;
    }

    @Deprecated
    void http(String url);

    @Deprecated
    void http(String url, Callback callback);

    @Deprecated
    void http(String url, Callback callback, HttpCredential credential);

    void http(HttpRequest request);

    void http(HttpRequest request, Callback2 callback);

    // endregion

    // region ping
    @Deprecated
    void ping(String domain);

    @Deprecated
    void ping(String domain, Callback callback);

    @Deprecated
    void ping(String domain, int size, Callback callback);

    @Deprecated
    void ping(String domain, int maxTimes, int timeout, Callback callback);

    @Deprecated
    void ping(String domain, int size, int maxTimes, int timeout, Callback callback);

    class PingRequest extends Request {
        public int size = DEFAULT_PING_SIZE;
        public int maxTimes = DEFAULT_MAX_TIMES;
        public int timeout = DEFAULT_TIMEOUT;
    }

    void ping(PingRequest pingRequest);

    void ping(PingRequest pingRequest, Callback2 callback);
    // endregion

    // region tcp ping
    @Deprecated
    void tcpPing(String domain, int port);

    @Deprecated
    void tcpPing(String domain, int port, Callback callback);

    @Deprecated
    void tcpPing(String domain, int port, int maxTimes, Callback callback);

    @Deprecated
    void tcpPing(String domain, int port, int maxTimes, int timeout, Callback callback);

    class TcpPingRequest extends PingRequest {
        public int port = INVALID;
    }

    void tcpPing(TcpPingRequest request);

    void tcpPing(TcpPingRequest request, Callback2 callback2);

    // endregion

    // region mtr
    @Deprecated
    void mtr(String domain);

    @Deprecated
    void mtr(String domain, Callback callback);

    @Deprecated
    void mtr(String domain, int maxTTL, Callback callback);

    @Deprecated
    void mtr(String domain, int maxTTL, int maxPaths, Callback callback);

    @Deprecated
    void mtr(String domain, int maxTTL, int maxPaths, int maxTimes, Callback callback);

    @Deprecated
    void mtr(String domain, int maxTTL, int maxPaths, int maxTimes, int timeout, Callback callback);

    class MtrRequest extends PingRequest {
        public int maxTTL = DEFAULT_MTR_MAX_TTL;
        public int maxPaths = DEFAULT_MTR_MAX_PATH;
    }

    void mtr(MtrRequest request);

    void mtr(MtrRequest request, Callback2 callback);
    // endregion

    // region dns
    @Deprecated
    void dns(String domain);

    @Deprecated
    void dns(String domain, Callback callback);

    @Deprecated
    void dns(String nameServer, String domain, Callback callback);

    @Deprecated
    void dns(String nameServer, String domain, String type, Callback callback);

    @Deprecated
    void dns(String nameServer, String domain, String type, int timeout, Callback callback);

    class DnsRequest extends PingRequest {
        public String type = DNS_TYPE_IPv4;
        public String nameServer;
    }

    void dns(DnsRequest dnsRequest);

    void dns(DnsRequest dnsRequest, Callback2 callback);
    // endregion
}
