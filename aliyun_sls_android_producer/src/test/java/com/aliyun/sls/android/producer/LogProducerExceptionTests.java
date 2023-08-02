package com.aliyun.sls.android.producer;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author yulong.gyl
 * @date 2023/8/2
 */
public class LogProducerExceptionTests {

    @Test
    public void logProducerException() {
        try {
            throw new LogProducerException();
        } catch (Exception e) {
            assertThat(e.getMessage()).isNull();
        }
    }

    @Test
    public void logProducerException_withArgs() {
        try {
            throw new LogProducerException("test message");
        } catch (Exception e) {
            assertThat(e.getMessage()).isEqualTo("test message");
        }
    }
}
