package com.aliyun.sls.android.network_diagnosis;

import com.aliyun.sls.android.network_diagnosis.NetworkDiagnosisFeature.NetworkDiagnosisHttpHeaderInjector;
import com.aliyun.sls.android.producer.BuildConfig;
import com.aliyun.sls.testable.BaseTestCase;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author yulong.gyl
 * @date 2023/8/18
 */
public class NetworkDiagnosisHttpHeaderInjectorTests extends BaseTestCase {
    @Test
    public void injectHeaders() {
        NetworkDiagnosisFeature feature = new NetworkDiagnosisFeature();
        NetworkDiagnosisHttpHeaderInjector injector = new NetworkDiagnosisHttpHeaderInjector(feature);
        assertThat(injector.injectHeaders(new String[] {"a", "b"}, 2)).isEqualTo(
            new String[] {
                "a",
                "b",
                "User-agent",
                "sls-android-sdk/" + BuildConfig.VERSION_NAME + ";" + feature.name() + "/" + feature.version()
            }
        );
    }
}
