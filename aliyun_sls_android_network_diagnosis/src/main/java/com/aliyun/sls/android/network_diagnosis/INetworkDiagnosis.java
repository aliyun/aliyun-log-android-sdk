package com.aliyun.sls.android.network_diagnosis;

import java.util.HashMap;
import java.util.Map;

/**
 * @author gordon
 * @date 2022/7/22
 */
public interface INetworkDiagnosis {

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

    // region setup
    void setPolicyDomain(String domain);

    void disableExNetworkInfo();

    /**
     * Enable the multiple ports detect. Default is false, use the default port.
     * @param enable true/false
     */
    void setMultiplePortsDetect(boolean enable);
    // endregion

    // region global callback
    void registerCallback(Callback callback);
    // endregion

    // region http
    void http(String url);

    void http(String url, Callback callback);
    // endregion

    // region ping
    void ping(String domain);

    void ping(String domain, Callback callback);

    void ping(String domain, int size, Callback callback);

    void ping(String domain, int maxTimes, int timeout, Callback callback);

    void ping(String domain, int size, int maxTimes, int timeout, Callback callback);
    // endregion

    // region tcp ping
    void tcpPing(String domain, int port);

    void tcpPing(String domain, int port, Callback callback);

    void tcpPing(String domain, int port, int maxTimes, Callback callback);

    void tcpPing(String domain, int port, int maxTimes, int timeout, Callback callback);

    // endregion

    // region mtr
    void mtr(String domain);

    void mtr(String domain, Callback callback);

    void mtr(String domain, int maxTTL, Callback callback);

    void mtr(String domain, int maxTTL, int maxPaths, Callback callback);

    void mtr(String domain, int maxTTL, int maxPaths, int maxTimes, Callback callback);

    void mtr(String domain, int maxTTL, int maxPaths, int maxTimes, int timeout, Callback callback);
    // endregion

    // region dns
    void dns(String domain);

    void dns(String domain, Callback callback);

    void dns(String nameServer, String domain, Callback callback);

    void dns(String nameServer, String domain, String type, Callback callback);

    void dns(String nameServer, String domain, String type, int timeout, Callback callback);
    // endregion
}
