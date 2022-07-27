package com.aliyun.sls.android.network_diagnosis;

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

    @Override
    public void disableExNetworkInfo() {
        if (checkNetworkDiagnosis()) {
            networkDiagnosis.disableExNetworkInfo();
        }
    }

    @Override
    public void setPolicyDomain(String domain) {
        if (checkNetworkDiagnosis()) {
            networkDiagnosis.setPolicyDomain(domain);
        }
    }

    @Override
    public void http(String url) {
        if (checkNetworkDiagnosis()) {
            networkDiagnosis.http(url);
        }
    }

    @Override
    public void ping(String domain) {
        if (checkNetworkDiagnosis()) {
            networkDiagnosis.ping(domain);
        }
    }

    @Override
    public void ping(String domain, int size) {
        if (checkNetworkDiagnosis()) {
            networkDiagnosis.ping(domain, size);
        }
    }

    @Override
    public void ping(String domain, int maxTimes, int timeout) {
        if (checkNetworkDiagnosis()) {
            networkDiagnosis.ping(domain, maxTimes, timeout);
        }
    }

    @Override
    public void ping(String domain, int size, int maxTimes, int timeout) {
        if (checkNetworkDiagnosis()) {
            networkDiagnosis.ping(domain, size, maxTimes, timeout);
        }
    }

    @Override
    public void tcpPing(String domain, int port) {
        if (checkNetworkDiagnosis()) {
            networkDiagnosis.tcpPing(domain, port);
        }
    }

    @Override
    public void tcpPing(String domain, int port, int maxTimes) {
        if (checkNetworkDiagnosis()) {
            networkDiagnosis.tcpPing(domain, port, maxTimes);
        }
    }

    @Override
    public void tcpPing(String domain, int port, int maxTimes, int timeout) {
        if (checkNetworkDiagnosis()) {
            networkDiagnosis.tcpPing(domain, port, maxTimes, timeout);
        }
    }

    @Override
    public void mtr(String domain) {
        if (checkNetworkDiagnosis()) {
            networkDiagnosis.mtr(domain);
        }
    }

    @Override
    public void mtr(String domain, int maxTTL) {
        if (checkNetworkDiagnosis()) {
            networkDiagnosis.mtr(domain, maxTTL);
        }
    }

    @Override
    public void mtr(String domain, int maxTTL, int maxPaths) {
        if (checkNetworkDiagnosis()) {
            networkDiagnosis.mtr(domain, maxTTL, maxPaths);
        }
    }

    @Override
    public void mtr(String domain, int maxTTL, int maxPaths, int maxTimes) {
        if (checkNetworkDiagnosis()) {
            networkDiagnosis.mtr(domain, maxTTL, maxPaths, maxTimes);
        }
    }

    @Override
    public void mtr(String domain, int maxTTL, int maxPaths, int maxTimes, int timeout) {
        if (checkNetworkDiagnosis()) {
            networkDiagnosis.mtr(domain, maxTTL, maxPaths, maxTimes, timeout);
        }
    }

    @Override
    public void dns(String nameServer, String domain) {
        if (checkNetworkDiagnosis()) {
            networkDiagnosis.dns(nameServer, domain);
        }
    }

    @Override
    public void dns(String nameServer, String domain, String type) {
        if (checkNetworkDiagnosis()) {
            networkDiagnosis.dns(nameServer, domain, type);
        }
    }

    @Override
    public void dns(String nameServer, String domain, String type, int timeout) {
        if (checkNetworkDiagnosis()) {
            networkDiagnosis.dns(nameServer, domain, type, timeout);
        }
    }
}
