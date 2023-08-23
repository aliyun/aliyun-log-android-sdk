package com.aliyun.sls.android.producer.utils;

import android.os.SystemClock;
import com.aliyun.sls.testable.BaseTestCase;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

/**
 * @author yulong.gyl
 * @date 2023/8/17
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({SystemClock.class})
public class TimeUtilsTests extends BaseTestCase {

    @Test
    public void timeUtils_RequestUrl() {
        final TimeUtils utils = TimeUtils.getInstance();
        final String endpoint = "https://cn-hangzhou.log.aliyuncs.com";
        final String project = "test-project";
        Assertions.assertThat(utils.getRequestUrl(endpoint, project)).isEqualTo(
            "https://test-project.cn-hangzhou.log.aliyuncs.com/servertime"
        );
    }

    @Test
    public void timeUtils_RequestHeader() {
        final TimeUtils utils = TimeUtils.getInstance();
        Assertions.assertThat(utils.getRequestHeader()).isEqualTo(new String[] {"x-log-apiversion", "0.6.0"});
    }

    @Test
    public void timeUtils_updateServerTime() {
        PowerMockito.mockStatic(SystemClock.class);
        Mockito.when(SystemClock.elapsedRealtime()).thenReturn(1692261377389L);
        final TimeUtils utils = TimeUtils.getInstance();
        utils.updateServerTime(1692261377399L);
        Assertions.assertThat(TimeUtils.serverTime).isEqualTo(1692261377399L);
        Assertions.assertThat(TimeUtils.elapsedRealTime).isEqualTo(1692261377389L);
        Assertions.assertThat(utils.getTimeInMillis()).isEqualTo(1692261377399L);
    }
}
