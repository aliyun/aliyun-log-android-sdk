package com.aliyun.sls.android.network_diagnosis;

import java.util.HashMap;

import com.aliyun.sls.android.network_diagnosis.INetworkDiagnosis.Callback;
import com.aliyun.sls.android.network_diagnosis.INetworkDiagnosis.Callback2;
import com.aliyun.sls.android.network_diagnosis.INetworkDiagnosis.DnsRequest;
import com.aliyun.sls.android.network_diagnosis.INetworkDiagnosis.HttpRequest;
import com.aliyun.sls.android.network_diagnosis.INetworkDiagnosis.MtrRequest;
import com.aliyun.sls.android.network_diagnosis.INetworkDiagnosis.PingRequest;
import com.aliyun.sls.android.network_diagnosis.INetworkDiagnosis.TcpPingRequest;
import com.aliyun.sls.testable.BaseTestCase;
import org.junit.BeforeClass;
import org.junit.Test;
import org.powermock.api.mockito.PowerMockito;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;

/**
 * @author yulong.gyl
 * @date 2023/8/17
 */
public class NetworkDiagnosisTests extends BaseTestCase {

    private static INetworkDiagnosis networkDiagnosisFeature;
    private static NetworkDiagnosis networkDiagnosis = NetworkDiagnosis.getInstance();

    @BeforeClass
    public static void beforeClass() {
        networkDiagnosisFeature = PowerMockito.mock(INetworkDiagnosis.class);
        networkDiagnosis.setNetworkDiagnosis(networkDiagnosisFeature);
    }

    @Test
    public void networkDiagnosis_checkNetworkDiagnosis() {
        networkDiagnosis.setNetworkDiagnosis(null);
        assertThat(networkDiagnosis.checkNetworkDiagnosis()).isFalse();

        networkDiagnosis.setNetworkDiagnosis(networkDiagnosisFeature);
        assertThat(networkDiagnosis.checkNetworkDiagnosis()).isTrue();
    }

    @Test
    public void networkDiagnosis_disableExNetworkInfo() {
        networkDiagnosis.disableExNetworkInfo();
        verify(networkDiagnosisFeature).disableExNetworkInfo();
    }

    @Test
    public void networkDiagnosis_setMultiplePortsDetect() {
        networkDiagnosis.setMultiplePortsDetect(true);
        verify(networkDiagnosisFeature).setMultiplePortsDetect(anyBoolean());
    }

    @Test
    public void networkDiagnosis_setPolicyDomain() {
        networkDiagnosis.setPolicyDomain("https://er.aliyuncs.com");
        verify(networkDiagnosisFeature).setPolicyDomain(anyString());
    }

    @Test
    public void networkDiagnosis_updateExtensions() {
        networkDiagnosis.updateExtensions(new HashMap<>());
        verify(networkDiagnosisFeature).updateExtensions(anyMap());
    }

    @Test
    public void networkDiagnosis_registerCallback() {
        networkDiagnosis.registerCallback((type, ret) -> {
        });
        verify(networkDiagnosisFeature).registerCallback((Callback)any());
    }

    @Test
    public void networkDiagnosis_registerCallback2() {
        networkDiagnosis.registerCallback(response -> {
        });
        verify(networkDiagnosisFeature).registerCallback((Callback2)any());
    }

    @Test
    public void networkDiagnosis_registerHttpCredentialCallback() {
        networkDiagnosis.registerHttpCredentialCallback((url, context) -> null);
        verify(networkDiagnosisFeature).registerHttpCredentialCallback(any());
    }

    @Test
    public void networkDiagnosis_http() {
        networkDiagnosis.http("url");
        verify(networkDiagnosisFeature).http(anyString());
    }

    @Test
    public void networkDiagnosis_http_callback() {
        networkDiagnosis.http("url", (type, ret) -> {
        });
        verify(networkDiagnosisFeature).http(anyString(), any());
    }

    @Test
    public void networkDiagnosis_http_callback_credential() {
        networkDiagnosis.http("url", (type, ret) -> {
        }, null);
        verify(networkDiagnosisFeature).http(anyString(), any(), any());
    }

    @Test
    public void networkDiagnosis_http_request() {
        networkDiagnosis.http(new HttpRequest());
        verify(networkDiagnosisFeature).http((HttpRequest)any());
    }

    @Test
    public void networkDiagnosis_http_request_callback() {
        networkDiagnosis.http(new HttpRequest(), null);
        verify(networkDiagnosisFeature).http((HttpRequest)any(), any());
    }

    @Test
    public void networkDiagnosis_ping() {
        networkDiagnosis.ping("url");
        verify(networkDiagnosisFeature).ping(anyString());
    }

    @Test
    public void networkDiagnosis_ping_callback() {
        networkDiagnosis.ping("url", (type, ret) -> {
        });
        verify(networkDiagnosisFeature).ping(anyString(), any());
    }

    @Test
    public void networkDiagnosis_ping_callback_size() {
        networkDiagnosis.ping("url", 10, null);
        verify(networkDiagnosisFeature).ping(anyString(), anyInt(), any());
    }

    @Test
    public void networkDiagnosis_ping_callback_size_maxTimes() {
        networkDiagnosis.ping("url", 10, 10, null);
        verify(networkDiagnosisFeature).ping(anyString(), anyInt(), anyInt(), any());
    }

    @Test
    public void networkDiagnosis_ping_callback_size_maxTimes_timeout() {
        networkDiagnosis.ping("url", 10, 10, 10, null);
        verify(networkDiagnosisFeature).ping(anyString(), anyInt(), anyInt(), anyInt(), any());
    }

    @Test
    public void networkDiagnosis_ping_request() {
        networkDiagnosis.ping(new PingRequest());
        verify(networkDiagnosisFeature).ping((PingRequest)any());
    }

    @Test
    public void networkDiagnosis_ping_request_callback() {
        networkDiagnosis.ping(new PingRequest(), null);
        verify(networkDiagnosisFeature).ping((PingRequest)any(), any());
    }

    @Test
    public void networkDiagnosis_tcpPing() {
        networkDiagnosis.tcpPing("url", 80);
        verify(networkDiagnosisFeature).tcpPing(anyString(), anyInt());
    }

    @Test
    public void networkDiagnosis_tcpPing_callback() {
        networkDiagnosis.tcpPing("url", 90, (type, ret) -> {
        });
        verify(networkDiagnosisFeature).tcpPing(anyString(), anyInt(), any());
    }

    @Test
    public void networkDiagnosis_tcpPing_callback_maxTimes() {
        networkDiagnosis.tcpPing("url", 10, 10, null);
        verify(networkDiagnosisFeature).tcpPing(anyString(), anyInt(), anyInt(), any());
    }

    @Test
    public void networkDiagnosis_tcpPing_callback_maxTimes_timeout() {
        networkDiagnosis.tcpPing("url", 10, 10, 10, null);
        verify(networkDiagnosisFeature).tcpPing(anyString(), anyInt(), anyInt(), anyInt(), any());
    }

    @Test
    public void networkDiagnosis_tcpPing_request() {
        networkDiagnosis.tcpPing(new TcpPingRequest());
        verify(networkDiagnosisFeature).tcpPing(any());
    }

    @Test
    public void networkDiagnosis_tcpPing_request_callback() {
        networkDiagnosis.tcpPing(new TcpPingRequest(), null);
        verify(networkDiagnosisFeature).tcpPing(any(), any());
    }

    @Test
    public void networkDiagnosis_mtr() {
        networkDiagnosis.mtr("url");
        verify(networkDiagnosisFeature).mtr(anyString());
    }

    @Test
    public void networkDiagnosis_mtr_callback() {
        networkDiagnosis.mtr("url", (type, ret) -> {
        });
        verify(networkDiagnosisFeature).mtr(anyString(), any());
    }

    @Test
    public void networkDiagnosis_mtr_callback_ttl() {
        networkDiagnosis.mtr("url", 10, null);
        verify(networkDiagnosisFeature).mtr(anyString(), anyInt(), any());
    }

    @Test
    public void networkDiagnosis_mtr_callback_ttl_maxPaths() {
        networkDiagnosis.mtr("url", 10, 10, null);
        verify(networkDiagnosisFeature).mtr(anyString(), anyInt(), anyInt(), any());
    }

    @Test
    public void networkDiagnosis_mtr_callback_size_maxPaths_maxTimes() {
        networkDiagnosis.mtr("url", 10, 10, 10, null);
        verify(networkDiagnosisFeature).mtr(anyString(), anyInt(), anyInt(), anyInt(), any());
    }

    @Test
    public void networkDiagnosis_mtr_callback_size_maxPaths_maxTimes_timeout() {
        networkDiagnosis.mtr("url", 10, 10, 10, 50, null);
        verify(networkDiagnosisFeature).mtr(anyString(), anyInt(), anyInt(), anyInt(), anyInt(), any());
    }

    @Test
    public void networkDiagnosis_mtr_request() {
        networkDiagnosis.mtr(new MtrRequest());
        verify(networkDiagnosisFeature).mtr((MtrRequest)any());
    }

    @Test
    public void networkDiagnosis_mtr_request_callback() {
        networkDiagnosis.mtr(new MtrRequest(), null);
        verify(networkDiagnosisFeature).mtr((MtrRequest)any(), any());
    }

    @Test
    public void networkDiagnosis_dns() {
        networkDiagnosis.dns("url");
        verify(networkDiagnosisFeature).dns(anyString());
    }

    @Test
    public void networkDiagnosis_dns_callback() {
        networkDiagnosis.dns("url", (type, ret) -> {
        });
        verify(networkDiagnosisFeature).dns(anyString(), any());
    }

    @Test
    public void networkDiagnosis_dns_callback_domain() {
        networkDiagnosis.dns("url", "domain", null);
        verify(networkDiagnosisFeature).dns(anyString(), anyString(), any());
    }

    @Test
    public void networkDiagnosis_dns_callback_domain_type() {
        networkDiagnosis.dns("url", "domain", "AAAA", null);
        verify(networkDiagnosisFeature).dns(anyString(), anyString(), anyString(), any());
    }

    @Test
    public void networkDiagnosis_dns_callback_domain_type_timeout() {
        networkDiagnosis.dns("url", "domain", "AAAA", 30, null);
        verify(networkDiagnosisFeature).dns(anyString(), anyString(), anyString(), anyInt(), any());
    }

    @Test
    public void networkDiagnosis_dns_request() {
        networkDiagnosis.dns(new DnsRequest());
        verify(networkDiagnosisFeature).dns((DnsRequest)any());
    }

    @Test
    public void networkDiagnosis_dns_request_callback() {
        networkDiagnosis.dns(new DnsRequest(), null);
        verify(networkDiagnosisFeature).dns((DnsRequest)any(), any());
    }
}
