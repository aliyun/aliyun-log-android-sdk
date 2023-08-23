package com.aliyun.sls.android.producer;

import com.aliyun.sls.testable.BaseTestCase;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author yulong.gyl
 * @date 2023/8/2
 */
public class HttpConfigProxyTests extends BaseTestCase {

    @BeforeClass
    public static void beforeClass() {
        assertThat(HttpConfigProxy.getUserAgent()).isEqualTo("sls-android-sdk/" + BuildConfig.VERSION_NAME);
    }

    @Test
    public void httpConfigProxy_addPluginUserAgent() {
        HttpConfigProxy.addPluginUserAgent("test", "2.3.4");
        assertThat(HttpConfigProxy.getUserAgent()).isEqualTo(
            "sls-android-sdk/" + BuildConfig.VERSION_NAME + ";" + "test/2.3.4");
    }

    @AfterClass
    public static void afterClass() {
        HttpConfigProxy.removePluginUserAgent("test");
    }
}
