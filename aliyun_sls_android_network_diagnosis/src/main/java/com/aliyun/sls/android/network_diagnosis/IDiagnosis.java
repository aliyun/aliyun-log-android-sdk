package com.aliyun.sls.android.network_diagnosis;

import java.util.Map;

import com.alibaba.netspeed.network.DnsConfig;
import com.alibaba.netspeed.network.HttpConfig;
import com.alibaba.netspeed.network.HttpCredentialCallback;
import com.alibaba.netspeed.network.Logger;
import com.alibaba.netspeed.network.MtrConfig;
import com.alibaba.netspeed.network.PingConfig;
import com.alibaba.netspeed.network.TcpPingConfig;

import io.opentelemetry.sdk.trace.SdkTracerProviderBuilder;

/**
 * @author gordon
 * @date 2023/4/6
 */
public interface IDiagnosis {

    void preInit(String secretKey, String deviceId, String siteId, Map<String, String> extension);

    void init(String secretKey, String deviceId, String siteId, Map<String, String> extension);

    void enableDebug(boolean debug);

    void registerLogger(Object context, Logger logger);

    void setPolicyDomain(String domain);

    void disableExNetworkInfo();

    void updateExtension(Map<String, String> extension);

    void registerHttpCredentialCallback(HttpCredentialCallback callback);

    void startHttpPing(HttpConfig config);

    void startPing(PingConfig config);

    void startTcpPing(TcpPingConfig config);

    void startMtr(MtrConfig config);

    void startDns(DnsConfig config);
}