package com.aliyun.sls.android.network_diagnosis;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.alibaba.netspeed.network.DnsConfig;
import com.alibaba.netspeed.network.HttpConfig;
import com.alibaba.netspeed.network.MtrConfig;
import com.alibaba.netspeed.network.PingConfig;
import com.alibaba.netspeed.network.TcpPingConfig;

import android.content.Context;
import com.aliyun.sls.android.core.configuration.Configuration;
import com.aliyun.sls.android.core.configuration.Credentials;
import com.aliyun.sls.android.network_diagnosis.INetworkDiagnosis.DnsRequest;
import com.aliyun.sls.android.network_diagnosis.INetworkDiagnosis.HttpCredential;
import com.aliyun.sls.android.network_diagnosis.INetworkDiagnosis.HttpRequest;
import com.aliyun.sls.android.network_diagnosis.INetworkDiagnosis.MtrRequest;
import com.aliyun.sls.android.network_diagnosis.INetworkDiagnosis.PingRequest;
import com.aliyun.sls.android.network_diagnosis.INetworkDiagnosis.TcpPingRequest;
import com.aliyun.sls.android.ot.Attribute;
import com.aliyun.sls.android.ot.ISpanProcessor;
import com.aliyun.sls.android.ot.ISpanProvider;
import com.aliyun.sls.android.ot.Resource;
import com.aliyun.sls.android.ot.Span;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * @author gordon
 * @date 2023/4/6
 */
public class NetworkDiagnosisFeatureTest {

    private NetworkDiagnosisFeature feature;
    private IDiagnosis diagnosis;

    {
        //MockedStatic<Diagnosis> diagnosisMockedStatic = mockStatic(Diagnosis.class);
        //diagnosisMockedStatic.when(()->Diagnosis.init(anyString(), anyString(), anyString(), anyMap()));
        //
        Context context = mock(Context.class);
        Credentials credentials = new Credentials();
        credentials.getNetworkDiagnosisCredentials();
        Configuration configuration = new Configuration(new ISpanProcessor() {
            @Override
            public boolean onEnd(Span span) {
                return false;
            }
        });
        configuration.spanProvider = new ISpanProvider() {
            @Override
            public Resource provideResource() {
                return null;
            }

            @Override
            public List<Attribute> provideAttribute() {
                return null;
            }
        };

        //feature = new NetworkDiagnosisFeature();
        feature = spy(NetworkDiagnosisFeature.class);
        diagnosis = spy(Diagnosis.class);
        feature.setDiagnosis(diagnosis);
        //feature.initialize(context, credentials, configuration);
    }

    @Before
    public void setup() {
        //MockedStatic<Diagnosis> diagnosisMockedStatic = mockStatic(Diagnosis.class);
        //doNothing().when(diagnosisMockedStatic.when(()->Diagnosis.startHttpPing(any())));
        //diagnosisMockedStatic.when(() -> Diagnosis.startHttpPing(any())).thenAnswer(invocation -> null);
        //diagnosisMockedStatic.when(Diagnosis::sta)
    }

    @After
    public void clear() {
        feature = null;
    }

    @Test
    public void name() {
        assertEquals("network_diagnosis", feature.name());
    }

    @Test
    public void version() {
        assertEquals(BuildConfig.VERSION_NAME, feature.version());
    }

    //@Test
    //public void newSpanBuilder() {
    //    String test = "ttttt";
    //    assertEquals(test, feature.newSpanBuilder(test).build().getName());
    //}

    @Test
    public void getIPAIdBySecretKey() {
        String ipaAppId = feature.getIPAIdBySecretKey(
            "eyJhbGl5dW5fdWlkIjoiMTIzNCIsImlwYV9hcHBfaWQiOiIxMjM0Iiwic2VjX2tleSI6IjEyMzQiLCJzaWduIjoiMTIzNCJ9");
        assertEquals("1234", ipaAppId);
    }

    @Test
    public void setPolicyDomain() {
        feature.setPolicyDomain("cn");
        verify(diagnosis, times(1)).setPolicyDomain("cn");

        feature.setPolicyDomain("");
        verify(diagnosis, times(0)).setPolicyDomain("");
    }

    @Test
    public void disableExNetworkInfo() {
        feature.disableExNetworkInfo();
        verify(diagnosis, times(1)).disableExNetworkInfo();
    }

    @Test
    public void setMultiplePortsDetect() {
        feature.setMultiplePortsDetect(true);
        assertTrue(feature.enableMultiplePortsDetect);
    }

    @Test
    public void updateExtensions() {
        feature.updateExtensions(null);
        verify(diagnosis, times(0)).updateExtension(null);

        @SuppressWarnings("unchecked")
        final ArgumentCaptor<Map<String, String>> argumentCaptor = ArgumentCaptor.forClass(Map.class);
        Map<String, String> map = new HashMap<String, String>(){
            {
                put("k1", "va1");
                put("k2", "va2");
            }
        };
        feature.updateExtensions(map);
        verify(diagnosis).updateExtension(argumentCaptor.capture());
        assertEquals(map, argumentCaptor.getValue());
    }

    @Test
    public void http_url() {
        final ResultCaptor<HttpConfig> resultCaptor = new ResultCaptor<>();
        doAnswer(resultCaptor).when(feature).createHttpConfigFromHttpRequest(any(), any());

        feature.http("https://www.aliyun.com");
        assertEquals("https://www.aliyun.com", resultCaptor.getResult().url);
    }

    @Test
    public void http_url_credential() {
        final ResultCaptor<HttpConfig> resultCaptor = new ResultCaptor<>();
        doAnswer(resultCaptor).when(feature).createHttpConfigFromHttpRequest(any(), any());

        feature.http("https://www.aliyun.com", null, new HttpCredential(null, null));
        assertEquals("https://www.aliyun.com", resultCaptor.getResult().url);
        assertNull(resultCaptor.getResult().httpCredential.getSslContext());
        assertNull(resultCaptor.getResult().httpCredential.getTrustManager());

    }

    @Test
    public void http_request() {
        final ResultCaptor<HttpConfig> resultCaptor = new ResultCaptor<>();
        doAnswer(resultCaptor).when(feature).createHttpConfigFromHttpRequest(any(), any());

        HttpRequest request = new HttpRequest();
        request.domain = "https://www.aliyun.com";
        request.ip = "10.0.0.1";
        request.timeout = 300;
        request.downloadBytesLimit = 1024;
        request.headerOnly = true;
        request.context = this;
        request.credential = new HttpCredential(null, null);

        feature.http(request);

        assertEquals(request.domain, resultCaptor.getResult().url);
        assertEquals(request.ip, resultCaptor.getResult().ip);
        assertEquals(request.timeout, resultCaptor.getResult().timeout);
        assertEquals(request.downloadBytesLimit, resultCaptor.getResult().downloadBytesLimit);
        assertEquals(request.headerOnly, resultCaptor.getResult().downloadHeaderOnly);
        assertEquals(request.context, resultCaptor.getResult().context);
        assertNull(resultCaptor.getResult().httpCredential.getSslContext());
        assertNull(resultCaptor.getResult().httpCredential.getTrustManager());
    }

    @Test
    public void ping_domain() {
        final ResultCaptor<PingConfig> resultCaptor = new ResultCaptor<>();
        doAnswer(resultCaptor).when(feature).createPingConfigFromPingRequest(any(), any());

        feature.ping("www.aliyun.com");

        assertEquals("www.aliyun.com", resultCaptor.getResult().domain);
    }

    @Test
    public void ping_domain_size() {
        final ResultCaptor<PingConfig> resultCaptor = new ResultCaptor<>();
        doAnswer(resultCaptor).when(feature).createPingConfigFromPingRequest(any(), any());

        feature.ping("www.aliyun.com", 2048, null);

        assertEquals("www.aliyun.com", resultCaptor.getResult().domain);
        assertEquals(2048, resultCaptor.getResult().getSize());
    }

    @Test
    public void ping_domain_maxTimes_timeout() {
        final ResultCaptor<PingConfig> resultCaptor = new ResultCaptor<>();
        doAnswer(resultCaptor).when(feature).createPingConfigFromPingRequest(any(), any());

        feature.ping("www.aliyun.com", 10, 55, null);

        assertEquals("www.aliyun.com", resultCaptor.getResult().domain);
        assertEquals(10, resultCaptor.getResult().maxTimes);
        assertEquals(55, resultCaptor.getResult().timeout);
    }

    @Test
    public void ping_domain_size_maxTimes_timeout() {
        final ResultCaptor<PingConfig> resultCaptor = new ResultCaptor<>();
        doAnswer(resultCaptor).when(feature).createPingConfigFromPingRequest(any(), any());

        feature.ping("www.aliyun.com", 88, 10, 55, null);

        assertEquals("www.aliyun.com", resultCaptor.getResult().domain);
        assertEquals(88, resultCaptor.getResult().getSize());
        assertEquals(10, resultCaptor.getResult().maxTimes);
        assertEquals(55, resultCaptor.getResult().timeout);
    }

    @Test
    public void ping_request() {
        final ResultCaptor<PingConfig> resultCaptor = new ResultCaptor<>();
        doAnswer(resultCaptor).when(feature).createPingConfigFromPingRequest(any(), any());

        PingRequest request = new PingRequest();
        request.domain = "www.aliyun.com";
        request.size = 13;
        request.maxTimes = 44;
        request.timeout = 23;
        request.context = this;

        feature.ping(request);

        assertEquals(request.domain, resultCaptor.getResult().domain);
        assertEquals(request.size, resultCaptor.getResult().getSize());
        assertEquals(request.maxTimes, resultCaptor.getResult().maxTimes);
        assertEquals(request.timeout, resultCaptor.getResult().timeout);
        assertEquals(request.context, resultCaptor.getResult().context);
    }

    @Test
    public void tcpPing_domain_port() {
        final ResultCaptor<TcpPingConfig> resultCaptor = new ResultCaptor<>();
        doAnswer(resultCaptor).when(feature).createTcpPingConfigFromTcpPingRequest(any(), any());

        feature.tcpPing("www.aliyun.com", 9099);

        assertEquals("www.aliyun.com", resultCaptor.getResult().domain);
        assertEquals(9099, resultCaptor.getResult().port);
    }

    @Test
    public void tcpPing_domain_port_maxTimes() {
        final ResultCaptor<TcpPingConfig> resultCaptor = new ResultCaptor<>();
        doAnswer(resultCaptor).when(feature).createTcpPingConfigFromTcpPingRequest(any(), any());

        feature.tcpPing("www.aliyun.com", 9099, 30, null);

        assertEquals("www.aliyun.com", resultCaptor.getResult().domain);
        assertEquals(9099, resultCaptor.getResult().port);
        assertEquals(30, resultCaptor.getResult().maxTimes);
    }

    @Test
    public void tcpPing_domain_port_maxTimes_timeout() {
        final ResultCaptor<TcpPingConfig> resultCaptor = new ResultCaptor<>();
        doAnswer(resultCaptor).when(feature).createTcpPingConfigFromTcpPingRequest(any(), any());

        feature.tcpPing("www.aliyun.com", 9099, 30, 3000, null);

        assertEquals("www.aliyun.com", resultCaptor.getResult().domain);
        assertEquals(9099, resultCaptor.getResult().port);
        assertEquals(30, resultCaptor.getResult().maxTimes);
        assertEquals(3000, resultCaptor.getResult().timeout);
    }

    @Test
    public void tcpPing_request() {
        final ResultCaptor<TcpPingConfig> resultCaptor = new ResultCaptor<>();
        doAnswer(resultCaptor).when(feature).createTcpPingConfigFromTcpPingRequest(any(), any());

        TcpPingRequest request = new TcpPingRequest();
        request.domain = "www.aliyun.com";
        request.port = 888;
        request.maxTimes = 44;
        request.timeout = 23;
        request.context = this;

        feature.tcpPing(request);

        assertEquals(request.domain, resultCaptor.getResult().domain);
        assertEquals(request.port, resultCaptor.getResult().port);
        assertEquals(request.maxTimes, resultCaptor.getResult().maxTimes);
        assertEquals(request.timeout, resultCaptor.getResult().timeout);
        assertEquals(request.context, resultCaptor.getResult().context);
    }

    @Test
    public void mtr_domain() {
        final ResultCaptor<MtrConfig> resultCaptor = new ResultCaptor<>();
        doAnswer(resultCaptor).when(feature).createMtrConfigFromMtrRequest(any(), any());

        feature.mtr("www.aliyun.com");

        assertEquals("www.aliyun.com", resultCaptor.getResult().domain);
    }

    @Test
    public void mtr_domain_maxTTL() {
        final ResultCaptor<MtrConfig> resultCaptor = new ResultCaptor<>();
        doAnswer(resultCaptor).when(feature).createMtrConfigFromMtrRequest(any(), any());

        feature.mtr("www.aliyun.com", 30, null);

        assertEquals("www.aliyun.com", resultCaptor.getResult().domain);
        assertEquals(30, resultCaptor.getResult().maxTtl);
    }

    @Test
    public void mtr_domain_maxTTL_maxPaths() {
        final ResultCaptor<MtrConfig> resultCaptor = new ResultCaptor<>();
        doAnswer(resultCaptor).when(feature).createMtrConfigFromMtrRequest(any(), any());

        feature.mtr("www.aliyun.com", 30, 10, null);

        assertEquals("www.aliyun.com", resultCaptor.getResult().domain);
        assertEquals(30, resultCaptor.getResult().maxTtl);
        assertEquals(10, resultCaptor.getResult().maxPaths);
    }

    @Test
    public void mtr_domain_maxTTL_maxPaths_maxTimes() {
        final ResultCaptor<MtrConfig> resultCaptor = new ResultCaptor<>();
        doAnswer(resultCaptor).when(feature).createMtrConfigFromMtrRequest(any(), any());

        feature.mtr("www.aliyun.com", 30, 10, 3, null);

        assertEquals("www.aliyun.com", resultCaptor.getResult().domain);
        assertEquals(30, resultCaptor.getResult().maxTtl);
        assertEquals(10, resultCaptor.getResult().maxPaths);
        assertEquals(3, resultCaptor.getResult().maxTimes);
    }

    @Test
    public void mtr_domain_maxTTL_maxPaths_maxTimes_timeout() {
        final ResultCaptor<MtrConfig> resultCaptor = new ResultCaptor<>();
        doAnswer(resultCaptor).when(feature).createMtrConfigFromMtrRequest(any(), any());

        feature.mtr("www.aliyun.com", 30, 10, 3, 1000, null);

        assertEquals("www.aliyun.com", resultCaptor.getResult().domain);
        assertEquals(30, resultCaptor.getResult().maxTtl);
        assertEquals(10, resultCaptor.getResult().maxPaths);
        assertEquals(3, resultCaptor.getResult().maxTimes);
        assertEquals(1000, resultCaptor.getResult().timeout);
    }

    @Test
    public void mtr_request() {
        final ResultCaptor<MtrConfig> resultCaptor = new ResultCaptor<>();
        doAnswer(resultCaptor).when(feature).createMtrConfigFromMtrRequest(any(), any());

        MtrRequest request = new MtrRequest();
        request.domain = "www.aliyun.com";
        request.maxTTL = 23;
        request.maxPaths = 12;
        request.maxTimes = 50;
        request.timeout = 89;
        request.context = this;

        //MtrConfig config = feature.createMtrConfigFromMtrRequest(request, null);
        feature.mtr(request);

        assertEquals(request.domain, resultCaptor.getResult().domain);
        assertEquals(request.maxTTL, resultCaptor.getResult().maxTtl);
        assertEquals(request.maxPaths, resultCaptor.getResult().maxPaths);
        assertEquals(request.maxTimes, resultCaptor.getResult().maxTimes);
        assertEquals(request.timeout, resultCaptor.getResult().timeout);
        assertEquals(request.context, resultCaptor.getResult().context);
    }

    @Test
    public void dns_domain() {
        final ResultCaptor<DnsConfig> resultCaptor = new ResultCaptor<>();
        doAnswer(resultCaptor).when(feature).createDnsConfigFromDnsRequest(any(), any());

        feature.dns("www.aliyun.com");

        assertEquals("www.aliyun.com", resultCaptor.getResult().domain);
    }

    @Test
    public void dns_domain_nameServer() {
        final ResultCaptor<DnsConfig> resultCaptor = new ResultCaptor<>();
        doAnswer(resultCaptor).when(feature).createDnsConfigFromDnsRequest(any(), any());

        feature.dns("113.113.113.113", "www.aliyun.com", null);

        assertEquals("www.aliyun.com", resultCaptor.getResult().domain);
        assertEquals("113.113.113.113", resultCaptor.getResult().server);
    }

    @Test
    public void dns_domain_nameServer_type() {
        final ResultCaptor<DnsConfig> resultCaptor = new ResultCaptor<>();
        doAnswer(resultCaptor).when(feature).createDnsConfigFromDnsRequest(any(), any());

        feature.dns("113.113.113.113", "www.aliyun.com", "AAAA", null);

        assertEquals("www.aliyun.com", resultCaptor.getResult().domain);
        assertEquals("113.113.113.113", resultCaptor.getResult().server);
        assertEquals("AAAA", resultCaptor.getResult().type);
    }

    @Test
    public void dns_request() {
        final ResultCaptor<DnsConfig> resultCaptor = new ResultCaptor<>();
        doAnswer(resultCaptor).when(feature).createDnsConfigFromDnsRequest(any(), any());

        DnsRequest request = new DnsRequest();
        request.nameServer = "113.113.113.113";
        request.domain = "www.aliyun.com";
        request.type = INetworkDiagnosis.DNS_TYPE_IPv6;
        request.timeout = 89;
        request.context = this;

        feature.dns(request);

        assertEquals(request.domain, resultCaptor.getResult().domain);
        assertEquals(request.nameServer, resultCaptor.getResult().server);
        assertEquals(request.type, resultCaptor.getResult().type);
        assertEquals(request.timeout, resultCaptor.getResult().timeout);
        assertEquals(request.context, resultCaptor.getResult().context);
    }

    private static class ResultCaptor<T> implements Answer<T> {
        private T result = null;

        public T getResult() {
            return result;
        }

        @Override
        public T answer(InvocationOnMock invocation) throws Throwable {
            //noinspection unchecked
            result = (T)invocation.callRealMethod();
            return result;
        }
    }

}
