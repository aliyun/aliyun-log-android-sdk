package com.aliyun.sls.android.network_diagnosis;

import android.content.Context;
import com.aliyun.sls.android.core.configuration.Credentials;
import com.aliyun.sls.android.core.configuration.Credentials.NetworkDiagnosisCredentials;
import com.aliyun.sls.android.network_diagnosis.NetworkDiagnosisFeature.NetworkDiagnosisSender;
import com.aliyun.sls.android.ot.Span;
import com.aliyun.sls.android.ot.SpanBuilder;
import com.aliyun.sls.testable.BaseTestCase;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.SuppressStaticInitializationFor;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author yulong.gyl
 * @date 2023/8/18
 */
@RunWith(PowerMockRunner.class)
@SuppressStaticInitializationFor("com.aliyun.sls.android.producer.LogProducerConfig")
//@PrepareForTest({System.class})
public class NetworkDiagnosisSenderTests extends BaseTestCase {

    private NetworkDiagnosisSender sender;
    private NetworkDiagnosisFeature feature;

    @BeforeClass
    public static void beforeClass() throws Exception {
        //PowerMockito.mockStatic(System.class);
        //PowerMockito.doNothing().when(System.class);
    }

    @Before
    public void setUp() throws Exception {
        Context context = mock(Context.class);
        feature = new NetworkDiagnosisFeature();
        sender = new NetworkDiagnosisSender(context, feature);
    }

    @After
    public void tearDown() throws Exception {
        feature = null;
        sender = null;
    }

    @Test
    public void networkDiagnosisSender_provideFeatureName() {
        assertThat(sender.provideFeatureName()).isEqualTo(feature.name());
    }

    @Test
    public void networkDiagnosisSender_provideLogFileName() {
        assertThat(sender.provideLogFileName()).isEqualTo("net_d");
    }

    @Test
    public void networkDiagnosisSender_provideEndpoint() {
        Credentials credentials = new Credentials();
        credentials.endpoint = "a";
        assertThat(sender.provideEndpoint(credentials)).isEqualTo("a");

        NetworkDiagnosisCredentials networkDiagnosisCredentials = credentials.getNetworkDiagnosisCredentials();
        networkDiagnosisCredentials.endpoint = "b";
        assertThat(sender.provideEndpoint(credentials)).isEqualTo("b");
    }

    @Test
    public void networkDiagnosisSender_provideProjectName() {
        Credentials credentials = new Credentials();
        credentials.project = "a";
        assertThat(sender.provideProjectName(credentials)).isEqualTo("a");

        NetworkDiagnosisCredentials networkDiagnosisCredentials = credentials.getNetworkDiagnosisCredentials();
        networkDiagnosisCredentials.project = "b";
        assertThat(sender.provideProjectName(credentials)).isEqualTo("b");
    }

    @Test
    public void networkDiagnosisSender_provideLogstoreName() {
        Credentials credentials = new Credentials();
        credentials.instanceId = "a";
        assertThat(sender.provideLogstoreName(credentials)).isNull();

        NetworkDiagnosisCredentials networkDiagnosisCredentials = credentials.getNetworkDiagnosisCredentials();
        networkDiagnosisCredentials.instanceId = "b";
        assertThat(sender.provideLogstoreName(credentials)).isEqualTo("ipa-b-raw");
    }

    @Test
    public void networkDiagnosisSender_provideAccessKeyId() {
        Credentials credentials = new Credentials();
        credentials.accessKeyId = "a";
        assertThat(sender.provideAccessKeyId(credentials)).isEqualTo("a");

        NetworkDiagnosisCredentials networkDiagnosisCredentials = credentials.getNetworkDiagnosisCredentials();
        networkDiagnosisCredentials.accessKeyId = "b";
        assertThat(sender.provideAccessKeyId(credentials)).isEqualTo("b");
    }

    @Test
    public void networkDiagnosisSender_provideAccessKeySecret() {
        Credentials credentials = new Credentials();
        credentials.accessKeySecret = "a";
        assertThat(sender.provideAccessKeySecret(credentials)).isEqualTo("a");

        NetworkDiagnosisCredentials networkDiagnosisCredentials = credentials.getNetworkDiagnosisCredentials();
        networkDiagnosisCredentials.accessKeySecret = "b";
        assertThat(sender.provideAccessKeySecret(credentials)).isEqualTo("b");
    }

    @Test
    public void networkDiagnosisSender_provideSecurityToken() {
        Credentials credentials = new Credentials();
        credentials.securityToken = "a";
        assertThat(sender.provideSecurityToken(credentials)).isEqualTo("a");

        NetworkDiagnosisCredentials networkDiagnosisCredentials = credentials.getNetworkDiagnosisCredentials();
        networkDiagnosisCredentials.securityToken = "b";
        assertThat(sender.provideSecurityToken(credentials)).isEqualTo("b");
    }

    @Test
    public void networkDiagnosisSender_createSpanBuilder() {
        NetworkDiagnosisFeature feature = mock(NetworkDiagnosisFeature.class);
        when(feature.newSpanBuilder(any())).thenReturn(new SpanBuilder("", null, null));

        assertThat(sender.createSpanBuilder(null, feature)).isNull();

        final String msg = "{\n"
            + "            \"method\": \"http\",\n"
            + "            \"url\": \"https://www.aliyun.com\",\n"
            + "            \"startDate\": 1690343146126,\n"
            + "            \"src\": \"app\"\n"
            + "        }";

        Span span = sender.createSpanBuilder(msg, feature).build();
        assertThat(span.getAttribute())
            .anyMatch(it -> it.key.equals("t") && it.value.equals("net_d"))
            .anyMatch(it -> it.key.equals("net.type") && it.value.equals("http"))
            .anyMatch(it -> it.key.equals("net.origin") && it.value.equals(msg));
    }
}
