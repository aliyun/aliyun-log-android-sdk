package com.aliyun.sls.android.network_diagnosis;

import java.util.Map;

/**
 * @author gordon
 * @date 2022/7/22
 */
@SuppressWarnings("deprecation")
public final class NetworkDiagnosis implements INetworkDiagnosis {
    private INetworkDiagnosis networkDiagnosis;

    private static class Holder {
        private static final NetworkDiagnosis INSTANCE = new NetworkDiagnosis();
    }

    /* package */
    void setNetworkDiagnosis(INetworkDiagnosis networkDiagnosis) {
        this.networkDiagnosis = networkDiagnosis;
    }

    public static NetworkDiagnosis getInstance() {
        return Holder.INSTANCE;
    }

    private boolean checkNetworkDiagnosis() {
        return null != this.networkDiagnosis;
    }

    // region configuration
    @Override
    public void disableExNetworkInfo() {
        if (checkNetworkDiagnosis()) {
            networkDiagnosis.disableExNetworkInfo();
        }
    }

    public void setMultiplePortsDetect(boolean enable) {
        if (checkNetworkDiagnosis()) {
            networkDiagnosis.setMultiplePortsDetect(enable);
        }
    }

    @Override
    public void setPolicyDomain(String domain) {
        if (checkNetworkDiagnosis()) {
            networkDiagnosis.setPolicyDomain(domain);
        }
    }

    @Override
    @Deprecated
    public void registerCallback(Callback callback) {
        if (checkNetworkDiagnosis()) {
            networkDiagnosis.registerCallback(callback);
        }
    }

    @Override
    public void registerCallback(Callback2 callback) {
        if (checkNetworkDiagnosis()) {
            networkDiagnosis.registerCallback(callback);
        }
    }

    @Override
    public void updateExtensions(Map<String, String> extension) {
        if (checkNetworkDiagnosis()) {
            networkDiagnosis.updateExtensions(extension);
        }
    }

    @Override
    public void registerHttpCredentialCallback(HttpCredentialCallback callback) {
        if (checkNetworkDiagnosis()) {
            networkDiagnosis.registerHttpCredentialCallback(callback);
        }
    }

    // endregion

    // region http
    @Deprecated
    @Override
    public void http(String url) {
        if (checkNetworkDiagnosis()) {
            networkDiagnosis.http(url);
        }
    }

    @Deprecated
    @Override
    public void http(String url, Callback callback) {
        if (checkNetworkDiagnosis()) {
            networkDiagnosis.http(url, callback);
        }
    }

    @Deprecated
    public void http(String url, Callback callback, HttpCredential credential) {
        if (checkNetworkDiagnosis()) {
            networkDiagnosis.http(url, callback, credential);
        }
    }

    @Override
    public void http(HttpRequest request) {
        if (checkNetworkDiagnosis()) {
            networkDiagnosis.http(request);
        }
    }

    @Override
    public void http(HttpRequest request, Callback2 callback) {
        if (checkNetworkDiagnosis()) {
            networkDiagnosis.http(request, callback);
        }
    }

    // endregion

    // region ping
    @Deprecated
    @Override
    public void ping(String domain) {
        if (checkNetworkDiagnosis()) {
            networkDiagnosis.ping(domain);
        }
    }

    @Deprecated
    @Override
    public void ping(String domain, Callback callback) {
        if (checkNetworkDiagnosis()) {
            networkDiagnosis.ping(domain, callback);
        }
    }

    @Deprecated
    @Override
    public void ping(String domain, int size, Callback callback) {
        if (checkNetworkDiagnosis()) {
            networkDiagnosis.ping(domain, size, callback);
        }
    }

    @Deprecated
    @Override
    public void ping(String domain, int maxTimes, int timeout, Callback callback) {
        if (checkNetworkDiagnosis()) {
            networkDiagnosis.ping(domain, maxTimes, timeout, callback);
        }
    }

    @Deprecated
    @Override
    public void ping(String domain, int size, int maxTimes, int timeout, Callback callback) {
        if (checkNetworkDiagnosis()) {
            networkDiagnosis.ping(domain, size, maxTimes, timeout, callback);
        }
    }

    @Override
    public void ping(PingRequest pingRequest) {
        if (checkNetworkDiagnosis()) {
            networkDiagnosis.ping(pingRequest);
        }
    }

    @Override
    public void ping(PingRequest pingRequest, Callback2 callback) {
        if (checkNetworkDiagnosis()) {
            networkDiagnosis.ping(pingRequest, callback);
        }
    }

    // endregion

    // region tcp ping
    @Deprecated
    @Override
    public void tcpPing(String domain, int port) {
        if (checkNetworkDiagnosis()) {
            networkDiagnosis.tcpPing(domain, port);
        }
    }

    @Override
    @Deprecated
    public void tcpPing(String domain, int port, Callback callback) {
        if (checkNetworkDiagnosis()) {
            networkDiagnosis.tcpPing(domain, port, callback);
        }
    }

    @Override
    @Deprecated
    public void tcpPing(String domain, int port, int maxTimes, Callback callback) {
        if (checkNetworkDiagnosis()) {
            networkDiagnosis.tcpPing(domain, port, maxTimes, callback);
        }
    }

    @Override
    @Deprecated
    public void tcpPing(String domain, int port, int maxTimes, int timeout, Callback callback) {
        if (checkNetworkDiagnosis()) {
            networkDiagnosis.tcpPing(domain, port, maxTimes, timeout, callback);
        }
    }

    @Override
    public void tcpPing(TcpPingRequest request) {
        if (checkNetworkDiagnosis()) {
            networkDiagnosis.tcpPing(request);
        }
    }

    @Override
    public void tcpPing(TcpPingRequest request, Callback2 callback2) {
        if (checkNetworkDiagnosis()) {
            networkDiagnosis.tcpPing(request, callback2);
        }
    }

    // endregion

    // region mtr
    @Override
    @Deprecated
    public void mtr(String domain) {
        if (checkNetworkDiagnosis()) {
            networkDiagnosis.mtr(domain);
        }
    }

    @Override
    @Deprecated
    public void mtr(String domain, Callback callback) {
        if (checkNetworkDiagnosis()) {
            networkDiagnosis.mtr(domain, callback);
        }
    }

    @Override
    @Deprecated
    public void mtr(String domain, int maxTTL, Callback callback) {
        if (checkNetworkDiagnosis()) {
            networkDiagnosis.mtr(domain, maxTTL, callback);
        }
    }

    @Override
    @Deprecated
    public void mtr(String domain, int maxTTL, int maxPaths, Callback callback) {
        if (checkNetworkDiagnosis()) {
            networkDiagnosis.mtr(domain, maxTTL, maxPaths, callback);
        }
    }

    @Override
    @Deprecated
    public void mtr(String domain, int maxTTL, int maxPaths, int maxTimes, Callback callback) {
        if (checkNetworkDiagnosis()) {
            networkDiagnosis.mtr(domain, maxTTL, maxPaths, maxTimes, callback);
        }
    }

    @Override
    @Deprecated
    public void mtr(String domain, int maxTTL, int maxPaths, int maxTimes, int timeout, Callback callback) {
        if (checkNetworkDiagnosis()) {
            networkDiagnosis.mtr(domain, maxTTL, maxPaths, maxTimes, timeout, callback);
        }
    }

    @Override
    public void mtr(MtrRequest request) {
        if (checkNetworkDiagnosis()) {
            networkDiagnosis.mtr(request);
        }
    }

    @Override
    public void mtr(MtrRequest request, Callback2 callback) {
        if (checkNetworkDiagnosis()) {
            networkDiagnosis.mtr(request, callback);
        }
    }

    // endregion

    // region dns
    @Override
    @Deprecated
    public void dns(String domain) {
        if (checkNetworkDiagnosis()) {
            networkDiagnosis.dns(domain);
        }
    }

    @Override
    @Deprecated
    public void dns(String domain, Callback callback) {
        if (checkNetworkDiagnosis()) {
            networkDiagnosis.dns(domain, callback);
        }
    }

    @Override
    @Deprecated
    public void dns(String nameServer, String domain, Callback callback) {
        if (checkNetworkDiagnosis()) {
            networkDiagnosis.dns(nameServer, domain, callback);
        }
    }

    @Override
    @Deprecated
    public void dns(String nameServer, String domain, String type, Callback callback) {
        if (checkNetworkDiagnosis()) {
            networkDiagnosis.dns(nameServer, domain, type, callback);
        }
    }

    @Override
    @Deprecated
    public void dns(String nameServer, String domain, String type, int timeout, Callback callback) {
        if (checkNetworkDiagnosis()) {
            networkDiagnosis.dns(nameServer, domain, type, timeout, callback);
        }
    }

    @Override
    public void dns(DnsRequest dnsRequest) {
        if (checkNetworkDiagnosis()) {
            networkDiagnosis.dns(dnsRequest);
        }
    }

    @Override
    public void dns(DnsRequest dnsRequest, Callback2 callback) {
        if (checkNetworkDiagnosis()) {
            networkDiagnosis.dns(dnsRequest, callback);
        }
    }
    // endregion
}
