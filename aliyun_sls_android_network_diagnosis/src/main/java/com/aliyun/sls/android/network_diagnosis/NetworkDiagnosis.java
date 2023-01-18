package com.aliyun.sls.android.network_diagnosis;

import java.util.Map;

/**
 * @author gordon
 * @date 2022/7/22
 */
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
    public void registerCallback(Callback callback) {
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

    // endregion

    // region http
    @Override
    public void http(String url) {
        if (checkNetworkDiagnosis()) {
            networkDiagnosis.http(url);
        }
    }

    @Override
    public void http(String url, Callback callback) {
        if (checkNetworkDiagnosis()) {
            networkDiagnosis.http(url, callback);
        }
    }
    // endregion

    // region ping
    @Override
    public void ping(String domain) {
        if (checkNetworkDiagnosis()) {
            networkDiagnosis.ping(domain);
        }
    }

    @Override
    public void ping(String domain, Callback callback) {
        if (checkNetworkDiagnosis()) {
            networkDiagnosis.ping(domain, callback);
        }
    }

    @Override
    public void ping(String domain, int size, Callback callback) {
        if (checkNetworkDiagnosis()) {
            networkDiagnosis.ping(domain, size, callback);
        }
    }

    @Override
    public void ping(String domain, int maxTimes, int timeout, Callback callback) {
        if (checkNetworkDiagnosis()) {
            networkDiagnosis.ping(domain, maxTimes, timeout, callback);
        }
    }

    @Override
    public void ping(String domain, int size, int maxTimes, int timeout, Callback callback) {
        if (checkNetworkDiagnosis()) {
            networkDiagnosis.ping(domain, size, maxTimes, timeout, callback);
        }
    }
    // endregion

    // region tcp ping
    @Override
    public void tcpPing(String domain, int port) {
        if (checkNetworkDiagnosis()) {
            networkDiagnosis.tcpPing(domain, port);
        }
    }

    @Override
    public void tcpPing(String domain, int port, Callback callback) {
        if (checkNetworkDiagnosis()) {
            networkDiagnosis.tcpPing(domain, port, callback);
        }
    }

    @Override
    public void tcpPing(String domain, int port, int maxTimes, Callback callback) {
        if (checkNetworkDiagnosis()) {
            networkDiagnosis.tcpPing(domain, port, maxTimes, callback);
        }
    }

    @Override
    public void tcpPing(String domain, int port, int maxTimes, int timeout, Callback callback) {
        if (checkNetworkDiagnosis()) {
            networkDiagnosis.tcpPing(domain, port, maxTimes, timeout, callback);
        }
    }
    // endregion

    // region mtr
    @Override
    public void mtr(String domain) {
        if (checkNetworkDiagnosis()) {
            networkDiagnosis.mtr(domain);
        }
    }

    @Override
    public void mtr(String domain, Callback callback) {
        if (checkNetworkDiagnosis()) {
            networkDiagnosis.mtr(domain, callback);
        }
    }

    @Override
    public void mtr(String domain, int maxTTL, Callback callback) {
        if (checkNetworkDiagnosis()) {
            networkDiagnosis.mtr(domain, maxTTL, callback);
        }
    }

    @Override
    public void mtr(String domain, int maxTTL, int maxPaths, Callback callback) {
        if (checkNetworkDiagnosis()) {
            networkDiagnosis.mtr(domain, maxTTL, maxPaths, callback);
        }
    }

    @Override
    public void mtr(String domain, int maxTTL, int maxPaths, int maxTimes, Callback callback) {
        if (checkNetworkDiagnosis()) {
            networkDiagnosis.mtr(domain, maxTTL, maxPaths, maxTimes, callback);
        }
    }

    @Override
    public void mtr(String domain, int maxTTL, int maxPaths, int maxTimes, int timeout, Callback callback) {
        if (checkNetworkDiagnosis()) {
            networkDiagnosis.mtr(domain, maxTTL, maxPaths, maxTimes, timeout, callback);
        }
    }
    // endregion

    // region dns
    @Override
    public void dns(String domain) {
        if (checkNetworkDiagnosis()) {
            networkDiagnosis.dns(domain);
        }
    }

    @Override
    public void dns(String domain, Callback callback) {
        if (checkNetworkDiagnosis()) {
            networkDiagnosis.dns(domain, callback);
        }
    }

    @Override
    public void dns(String nameServer, String domain, Callback callback) {
        if (checkNetworkDiagnosis()) {
            networkDiagnosis.dns(nameServer, domain, callback);
        }
    }

    @Override
    public void dns(String nameServer, String domain, String type, Callback callback) {
        if (checkNetworkDiagnosis()) {
            networkDiagnosis.dns(nameServer, domain, type, callback);
        }
    }

    @Override
    public void dns(String nameServer, String domain, String type, int timeout, Callback callback) {
        if (checkNetworkDiagnosis()) {
            networkDiagnosis.dns(nameServer, domain, type, timeout, callback);
        }
    }
    // endregion
}
