package com.aliyun.sls.android.producer.internal;

import com.aliyun.sls.android.producer.BuildConfig;
import com.aliyun.sls.testable.BaseTestCase;
import org.assertj.core.api.Assertions;
import org.junit.Test;

/**
 * @author yulong.gyl
 * @date 2023/8/17
 */
public class HttpHeaderTests extends BaseTestCase {

    @Test
    public void httpHeader_getHeadersWithUA() {
        String[] srcHeaders = new String[] {"srcKey", "srcValue"};
        Assertions.assertThat(HttpHeader.getHeadersWithUA(srcHeaders)).isEqualTo(
            new String[] {"srcKey", "srcValue", "User-agent", "sls-android-sdk/" + BuildConfig.VERSION_NAME}
        );

        Assertions.assertThat(HttpHeader.getHeadersWithUA(srcHeaders, "k", "v")).isEqualTo(
            new String[] {"srcKey", "srcValue", "User-agent", "sls-android-sdk/" + BuildConfig.VERSION_NAME + ";k;v"}
        );
    }
}
