package com.aliyun.sls.android.network_diagnosis;

/**
 * @author gordon
 * @date 2022/7/22
 */
public interface INetworkDiagnosis {

    enum Type {
        HTTP,
        PING,
        TCPPING,
        MTR,
        DNS
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
    // end

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
