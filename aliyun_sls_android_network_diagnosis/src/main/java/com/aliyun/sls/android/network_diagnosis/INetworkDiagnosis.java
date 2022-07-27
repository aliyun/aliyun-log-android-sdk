package com.aliyun.sls.android.network_diagnosis;

/**
 * @author gordon
 * @date 2022/7/22
 */
public interface INetworkDiagnosis {

    // region setup
    void setPolicyDomain(String domain);

    void disableExNetworkInfo();
    // end

    // region http
    void http(String url);
    // endregion

    // region ping
    void ping(String domain);

    void ping(String domain, int size);

    void ping(String domain, int maxTimes, int timeout);

    void ping(String domain, int size, int maxTimes, int timeout);
    // endregion

    // region tcp ping
    void tcpPing(String domain, int port);

    void tcpPing(String domain, int port, int maxTimes);

    void tcpPing(String domain, int port, int maxTimes, int timeout);

    // endregion

    // region mtr
    void mtr(String domain);

    void mtr(String domain, int maxTTL);

    void mtr(String domain, int maxTTL, int maxPaths);

    void mtr(String domain, int maxTTL, int maxPaths, int maxTimes);

    void mtr(String domain, int maxTTL, int maxPaths, int maxTimes, int timeout);

    // endregion

    // region dns
    void dns(String nameServer, String domain);

    void dns(String nameServer, String domain, String type);

    void dns(String nameServer, String domain, String type, int timeout);
    // endregion
}
