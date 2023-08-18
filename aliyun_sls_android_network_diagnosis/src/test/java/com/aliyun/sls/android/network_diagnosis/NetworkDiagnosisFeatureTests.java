package com.aliyun.sls.android.network_diagnosis;

import java.util.HashMap;
import java.util.Map;

import com.alibaba.netspeed.network.DnsConfig;
import com.alibaba.netspeed.network.HttpConfig;
import com.alibaba.netspeed.network.MtrConfig;
import com.alibaba.netspeed.network.PingConfig;
import com.alibaba.netspeed.network.TcpPingConfig;

import android.content.Context;
import com.aliyun.sls.android.core.utdid.Utdid;
import com.aliyun.sls.android.network_diagnosis.INetworkDiagnosis.Callback;
import com.aliyun.sls.android.network_diagnosis.INetworkDiagnosis.Callback2;
import com.aliyun.sls.android.network_diagnosis.INetworkDiagnosis.DnsRequest;
import com.aliyun.sls.android.network_diagnosis.INetworkDiagnosis.HttpCredential;
import com.aliyun.sls.android.network_diagnosis.INetworkDiagnosis.HttpRequest;
import com.aliyun.sls.android.network_diagnosis.INetworkDiagnosis.MtrRequest;
import com.aliyun.sls.android.network_diagnosis.INetworkDiagnosis.PingRequest;
import com.aliyun.sls.android.network_diagnosis.INetworkDiagnosis.Response;
import com.aliyun.sls.android.network_diagnosis.INetworkDiagnosis.TcpPingRequest;
import com.aliyun.sls.testable.BaseTestCase;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author yulong.gyl
 * @date 2023/8/17
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({Utdid.class})
public class NetworkDiagnosisFeatureTests extends BaseTestCase {

    private IDiagnosis diagnosis;
    private NetworkDiagnosisFeature feature;
    private Context context;

    @Before
    public void setUp() throws Exception {
        diagnosis = mock(IDiagnosis.class);
        context = mock(Context.class);
        Utdid utdid = mock(Utdid.class);
        when(utdid.getUtdid(context)).thenReturn("123456");

        feature = new NetworkDiagnosisFeature();
        feature.setDiagnosis(diagnosis);
        feature.setUtdid(utdid);
    }

    @After
    public void tearDown() throws Exception {
        feature.setUtdid(null);
        feature.setDiagnosis(null);
        feature = null;
        context = null;
        diagnosis = null;
    }

    @Test
    public void networkDiagnosisFeature_name() {
        assertThat(feature.name()).isEqualTo("network_diagnosis");
    }

    @Test
    public void networkDiagnosisFeature_version() {
        assertThat(feature.version()).isEqualTo(BuildConfig.VERSION_NAME);
    }

    @Test
    public void networkDiagnosisFeature_getIPAIdBySecretKey() {
        final String secretKey
            =
            "eyJhbGl5dW5fdWlkIjoiMTExMTExMTExMSIsImlwYV9hcHBfaWQiOiIyMjIyMjIyMjIyIiwic2VjX2tleSI6IjMzMzMzMzMzMzMiLCJzaWduIjoiNDQ0NDQ0NDQ0NCJ9";
        assertThat(feature.getIPAIdBySecretKey(secretKey)).isEqualTo("2222222222");

        assertThat(feature.getIPAIdBySecretKey("")).isEqualTo("");
    }

    //@Test
    //public void networkDiagnosisFeature_preInit() {
    //    Credentials credentials = new Credentials();
    //    credentials.networkDiagnosisCredentials = credentials.getNetworkDiagnosisCredentials();
    //    Configuration configuration = new Configuration(null);
    //
    //    feature.onPreInit(context, credentials, configuration);
    //    verify(diagnosis).preInit(anyString(), anyString(), anyString(), anyMap());
    //    verify(diagnosis).registerLogger(any(), any());
    //    assertThat(NetworkDiagnosis.getInstance().networkDiagnosis).isEqualTo(feature);
    //}

    @Test
    public void networkDiagnosisFeature_setPolicyDomain() {
        feature.setPolicyDomain("https://www.domain.cn");
        verify(diagnosis).setPolicyDomain("https://www.domain.cn");

        feature.setPolicyDomain("");
        verify(diagnosis, never()).setPolicyDomain("");
    }

    @Test
    public void networkDiagnosisFeature_disableExNetworkInfo() {
        feature.disableExNetworkInfo();
        verify(diagnosis).disableExNetworkInfo();
    }

    @Test
    public void networkDiagnosisFeature_setMultiplePortsDetect() {
        feature.setMultiplePortsDetect(true);
        assertThat(feature.enableMultiplePortsDetect).isTrue();
    }

    @Test
    public void networkDiagnosisFeature_updateExtensions() {
        Map<String, String> extensions = new HashMap<String, String>() {
            {
                put("key", "value");
            }
        };
        feature.updateExtensions(extensions);
        verify(diagnosis).updateExtension(extensions);

        feature.updateExtensions(null);
        verify(diagnosis, never()).updateExtension(null);
    }

    @Test
    public void networkDiagnosisFeature_registerHttpCredentialCallback() {
        feature.registerHttpCredentialCallback((url, context) -> null);
        verify(diagnosis).registerHttpCredentialCallback(any());
    }

    // region http
    @Test
    public void networkDiagnosisFeature_http() {
        feature.http("https://www.aliyun.com");
        verify(diagnosis).startHttpPing(argThat(argument -> "https://www.aliyun.com".equals(argument.url)));
    }

    @Test
    public void networkDiagnosisFeature_http_credentials() {
        final Callback callback = (type, ret) -> {
        };
        final HttpCredential credential = new HttpCredential(null, null);
        feature.http("https://www.aliyun.com", callback, credential);
        verify(diagnosis).startHttpPing(
            argThat(argument -> "https://www.aliyun.com".equals(argument.url) && argument.httpCredential != null)
        );
    }

    @Test
    public void networkDiagnosisFeature_http_request() {
        final HttpCredential credential = new HttpCredential(null, null);
        final HttpRequest request = new HttpRequest();
        request.domain = "https://www.aliyun.com";
        request.credential = credential;
        request.ip = "10.0.0.0";
        request.timeout = 10;
        request.downloadBytesLimit = 20;
        request.headerOnly = false;
        feature.http(request);

        ArgumentCaptor<HttpConfig> captor = ArgumentCaptor.forClass(HttpConfig.class);
        verify(diagnosis).startHttpPing(captor.capture());
        assertThat(captor.getValue().url).isEqualTo("https://www.aliyun.com");
        assertThat(captor.getValue().httpCredential).isNotNull();
        assertThat(captor.getValue().ip).isEqualTo("10.0.0.0");
        assertThat(captor.getValue().timeout).isEqualTo(10);
        assertThat(captor.getValue().downloadBytesLimit).isEqualTo(20);
        assertThat(captor.getValue().downloadHeaderOnly).isFalse();
    }
    // endregion

    // region ping
    @Test
    public void networkDiagnosisFeature_ping() {
        feature.ping("www.aliyun.com");
        verify(diagnosis).startPing(argThat(argument -> "www.aliyun.com".equals(argument.domain)));
    }

    @Test
    public void networkDiagnosisFeature_ping_maxTimes_timeout() {
        final Callback callback = (type, ret) -> {
        };
        feature.ping("www.aliyun.com", 15, 300, callback);

        ArgumentCaptor<PingConfig> captor = ArgumentCaptor.forClass(PingConfig.class);
        verify(diagnosis).startPing(captor.capture());
        assertThat(captor.getValue().domain).isEqualTo("www.aliyun.com");
        assertThat(captor.getValue().getSize()).isEqualTo(64);
        assertThat(captor.getValue().timeout).isEqualTo(300);
        assertThat(captor.getValue().maxTimes).isEqualTo(15);
    }

    @Test
    public void networkDiagnosisFeature_ping_size_maxTimes_timeout() {
        final Callback callback = (type, ret) -> {
        };
        feature.ping("www.aliyun.com", 10, 25, 300, callback);

        ArgumentCaptor<PingConfig> captor = ArgumentCaptor.forClass(PingConfig.class);
        verify(diagnosis).startPing(captor.capture());
        assertThat(captor.getValue().domain).isEqualTo("www.aliyun.com");
        assertThat(captor.getValue().getSize()).isEqualTo(10);
        assertThat(captor.getValue().timeout).isEqualTo(300);
        assertThat(captor.getValue().maxTimes).isEqualTo(25);
    }

    @Test
    public void networkDiagnosisFeature_ping_request() {
        final PingRequest request = new PingRequest();
        request.size = 10;
        request.domain = "aliyun.com";
        request.maxTimes = 20;
        request.timeout = 100;
        feature.ping(request);

        ArgumentCaptor<PingConfig> captor = ArgumentCaptor.forClass(PingConfig.class);
        verify(diagnosis).startPing(captor.capture());
        assertThat(captor.getValue().domain).isEqualTo("aliyun.com");
        assertThat(captor.getValue().getSize()).isEqualTo(10);
        assertThat(captor.getValue().timeout).isEqualTo(100);
        assertThat(captor.getValue().maxTimes).isEqualTo(20);
    }
    // endregion

    // region tcpping
    @Test
    public void networkDiagnosisFeature_tcpping() {
        feature.tcpPing("www.aliyun.com", 80);
        verify(diagnosis).startTcpPing(
            argThat(argument -> "www.aliyun.com".equals(argument.domain) && argument.port == 80)
        );
    }

    @Test
    public void networkDiagnosisFeature_tcpping_size_maxTimes() {
        final Callback callback = (type, ret) -> {
        };
        feature.tcpPing("www.aliyun.com", 10, 25, callback);

        ArgumentCaptor<TcpPingConfig> captor = ArgumentCaptor.forClass(TcpPingConfig.class);
        verify(diagnosis).startTcpPing(captor.capture());
        assertThat(captor.getValue().domain).isEqualTo("www.aliyun.com");
        assertThat(captor.getValue().port).isEqualTo(10);
        assertThat(captor.getValue().maxTimes).isEqualTo(25);
    }

    @Test
    public void networkDiagnosisFeature_tcpping_size_maxTimes_timeout() {
        final Callback callback = (type, ret) -> {
        };
        feature.tcpPing("www.aliyun.com", 10, 25, 300, callback);

        ArgumentCaptor<TcpPingConfig> captor = ArgumentCaptor.forClass(TcpPingConfig.class);
        verify(diagnosis).startTcpPing(captor.capture());
        assertThat(captor.getValue().domain).isEqualTo("www.aliyun.com");
        assertThat(captor.getValue().port).isEqualTo(10);
        assertThat(captor.getValue().maxTimes).isEqualTo(25);
        assertThat(captor.getValue().timeout).isEqualTo(300);
    }

    @Test
    public void networkDiagnosisFeature_tcpping_Request() {
        TcpPingRequest request = new TcpPingRequest();
        request.domain = "www.aliyun.com";
        request.port = 10;
        request.maxTimes = 25;
        request.timeout = 300;
        feature.tcpPing(request);

        ArgumentCaptor<TcpPingConfig> captor = ArgumentCaptor.forClass(TcpPingConfig.class);
        verify(diagnosis).startTcpPing(captor.capture());
        assertThat(captor.getValue().domain).isEqualTo("www.aliyun.com");
        assertThat(captor.getValue().port).isEqualTo(10);
        assertThat(captor.getValue().maxTimes).isEqualTo(25);
        assertThat(captor.getValue().timeout).isEqualTo(300);
    }
    // endregion

    // region mtr
    @Test
    public void networkDiagnosisFeature_mtr() {
        feature.mtr("www.aliyun.com");
        verify(diagnosis).startMtr(
            argThat(argument -> "www.aliyun.com".equals(argument.domain))
        );
    }

    @Test
    public void networkDiagnosisFeature_mtr_maxTTL() {
        final Callback callback = (type, ret) -> {
        };
        feature.mtr("www.aliyun.com", 10, callback);

        ArgumentCaptor<MtrConfig> captor = ArgumentCaptor.forClass(MtrConfig.class);
        verify(diagnosis).startMtr(captor.capture());
        assertThat(captor.getValue().domain).isEqualTo("www.aliyun.com");
        assertThat(captor.getValue().maxTtl).isEqualTo(10);
    }

    @Test
    public void networkDiagnosisFeature_mtr_maxTTL_maxPaths() {
        final Callback callback = (type, ret) -> {
        };
        feature.mtr("www.aliyun.com", 10, 25, callback);

        ArgumentCaptor<MtrConfig> captor = ArgumentCaptor.forClass(MtrConfig.class);
        verify(diagnosis).startMtr(captor.capture());
        assertThat(captor.getValue().domain).isEqualTo("www.aliyun.com");
        assertThat(captor.getValue().maxTtl).isEqualTo(10);
        assertThat(captor.getValue().maxPaths).isEqualTo(25);
    }

    @Test
    public void networkDiagnosisFeature_mtr_maxTTL_maxPaths_maxTimes() {
        final Callback callback = (type, ret) -> {
        };
        feature.mtr("www.aliyun.com", 10, 25, 300, callback);

        ArgumentCaptor<MtrConfig> captor = ArgumentCaptor.forClass(MtrConfig.class);
        verify(diagnosis).startMtr(captor.capture());
        assertThat(captor.getValue().domain).isEqualTo("www.aliyun.com");
        assertThat(captor.getValue().maxTtl).isEqualTo(10);
        assertThat(captor.getValue().maxPaths).isEqualTo(25);
        assertThat(captor.getValue().maxTimes).isEqualTo(300);
    }

    @Test
    public void networkDiagnosisFeature_mtr_maxTTL_maxPaths_maxTimes_timeout() {
        final Callback callback = (type, ret) -> {
        };
        feature.mtr("www.aliyun.com", 10, 25, 300, 200, callback);

        ArgumentCaptor<MtrConfig> captor = ArgumentCaptor.forClass(MtrConfig.class);
        verify(diagnosis).startMtr(captor.capture());
        assertThat(captor.getValue().domain).isEqualTo("www.aliyun.com");
        assertThat(captor.getValue().maxTtl).isEqualTo(10);
        assertThat(captor.getValue().maxPaths).isEqualTo(25);
        assertThat(captor.getValue().maxTimes).isEqualTo(300);
        assertThat(captor.getValue().timeout).isEqualTo(200);
    }

    @Test
    public void networkDiagnosisFeature_mtr_Request() {
        MtrRequest request = new MtrRequest();
        request.domain = "www.aliyun.com";
        request.maxTTL = 10;
        request.maxPaths = 25;
        request.maxTimes = 15;
        request.timeout = 300;
        request.extension = new HashMap<String, String>() {};
        request.multiplePortsDetect = true;

        feature.mtr(request);

        ArgumentCaptor<MtrConfig> captor = ArgumentCaptor.forClass(MtrConfig.class);
        verify(diagnosis).startMtr(captor.capture());
        assertThat(captor.getValue().domain).isEqualTo("www.aliyun.com");
        assertThat(captor.getValue().maxTtl).isEqualTo(10);
        assertThat(captor.getValue().maxPaths).isEqualTo(25);
        assertThat(captor.getValue().maxTimes).isEqualTo(15);
        assertThat(captor.getValue().timeout).isEqualTo(300);
        assertThat(captor.getValue().detectExtension).isNotNull();
        assertThat(captor.getValue().multiplePortsDetect).isTrue();
    }
    // endregion

    // region dns
    @Test
    public void networkDiagnosisFeature_dns() {
        feature.dns("www.aliyun.com");
        verify(diagnosis).startDns(
            argThat(argument -> "www.aliyun.com".equals(argument.domain))
        );
    }

    @Test
    public void networkDiagnosisFeature_dns_nameServer() {
        final Callback callback = (type, ret) -> {
        };
        feature.dns("www.aliyun.com", "www.nameserver.com", callback);

        ArgumentCaptor<DnsConfig> captor = ArgumentCaptor.forClass(DnsConfig.class);
        verify(diagnosis).startDns(captor.capture());
        assertThat(captor.getValue().server).isEqualTo("www.aliyun.com");
        assertThat(captor.getValue().domain).isEqualTo("www.nameserver.com");
    }

    @Test
    public void networkDiagnosisFeature_dns_nameServer_type() {
        final Callback callback = (type, ret) -> {
        };
        feature.dns("www.aliyun.com", "www.nameserver.com", "AAAA", callback);

        ArgumentCaptor<DnsConfig> captor = ArgumentCaptor.forClass(DnsConfig.class);
        verify(diagnosis).startDns(captor.capture());
        assertThat(captor.getValue().server).isEqualTo("www.aliyun.com");
        assertThat(captor.getValue().domain).isEqualTo("www.nameserver.com");
        assertThat(captor.getValue().type).isEqualTo("AAAA");
    }

    @Test
    public void networkDiagnosisFeature_dns_Request() {
        DnsRequest request = new DnsRequest();
        request.domain = "www.aliyun.com";
        request.nameServer = "www.nameserver.com";
        request.type = "AAAA";
        request.timeout = 300;
        request.multiplePortsDetect = true;
        request.extension = new HashMap<String, String>() {};

        feature.dns(request);

        ArgumentCaptor<DnsConfig> captor = ArgumentCaptor.forClass(DnsConfig.class);
        verify(diagnosis).startDns(captor.capture());
        assertThat(captor.getValue().domain).isEqualTo("www.aliyun.com");
        assertThat(captor.getValue().server).isEqualTo("www.nameserver.com");
        assertThat(captor.getValue().type).isEqualTo("AAAA");
        assertThat(captor.getValue().timeout).isEqualTo(300);
        assertThat(captor.getValue().multiplePortsDetect).isTrue();
        assertThat(captor.getValue().detectExtension).isNotNull();
    }
    // endregion
}
