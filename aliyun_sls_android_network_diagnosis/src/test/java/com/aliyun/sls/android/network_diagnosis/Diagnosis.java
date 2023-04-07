package com.aliyun.sls.android.network_diagnosis;

import java.util.Map;

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
public class Diagnosis implements IDiagnosis{
    @Override
    public void init(String secretKey, String deviceId, String siteId, Map<String, String> extension) {

    }

    @Override
    public void enableDebug(boolean debug) {

    }

    @Override
    public void registerLogger(Object context, Logger logger) {

    }

    @Override
    public void setPolicyDomain(String domain) {

    }

    @Override
    public void disableExNetworkInfo() {

    }

    @Override
    public void updateExtension(Map<String, String> extension) {

    }

    @Override
    public void registerHttpCredentialCallback(HttpCredentialCallback callback) {

    }

    @Override
    public void startHttpPing(HttpConfig config) {

    }

    @Override
    public void startPing(PingConfig config) {

    }

    @Override
    public void startTcpPing(TcpPingConfig config) {

    }

    @Override
    public void startMtr(MtrConfig config) {

    }

    @Override
    public void startDns(DnsConfig config) {

    }
}
