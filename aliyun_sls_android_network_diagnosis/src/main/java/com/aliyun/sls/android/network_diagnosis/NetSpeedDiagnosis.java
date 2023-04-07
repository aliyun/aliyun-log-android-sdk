package com.aliyun.sls.android.network_diagnosis;

import java.util.Map;

import com.alibaba.netspeed.network.Diagnosis;
import com.alibaba.netspeed.network.DnsConfig;
import com.alibaba.netspeed.network.HttpConfig;
import com.alibaba.netspeed.network.HttpCredentialCallback;
import com.alibaba.netspeed.network.Logger;
import com.alibaba.netspeed.network.MtrConfig;
import com.alibaba.netspeed.network.PingConfig;
import com.alibaba.netspeed.network.TcpPingConfig;

/**
 * @author gordon
 * @date 2023/4/6
 */
public class NetSpeedDiagnosis implements IDiagnosis {
    @Override
    public void init(String secretKey, String deviceId, String siteId, Map<String, String> extension) {
        Diagnosis.init(secretKey, deviceId, siteId, extension);
    }

    @Override
    public void enableDebug(boolean debug) {
        Diagnosis.enableDebug(debug);
    }

    @Override
    public void registerLogger(Object context, Logger logger) {
        Diagnosis.registerLogger(context, logger);
    }

    @Override
    public void setPolicyDomain(String domain) {
        Diagnosis.setPolicyDomain(domain);
    }

    @Override
    public void disableExNetworkInfo() {
        Diagnosis.disableExNetworkInfo();
    }

    @Override
    public void updateExtension(Map<String, String> extension) {
        Diagnosis.updateExtension(extension);
    }

    @Override
    public void registerHttpCredentialCallback(HttpCredentialCallback callback) {
        Diagnosis.registerHttpCredentialCallback(callback);
    }

    @Override
    public void startHttpPing(HttpConfig config) {
        Diagnosis.startHttpPing(config);
    }

    @Override
    public void startPing(PingConfig config) {
        Diagnosis.startPing(config);
    }

    @Override
    public void startTcpPing(TcpPingConfig config) {
        Diagnosis.startTcpPing(config);
    }

    @Override
    public void startMtr(MtrConfig config) {
        Diagnosis.startMtr(config);
    }

    @Override
    public void startDns(DnsConfig config) {
        Diagnosis.startDns(config);
    }
}
