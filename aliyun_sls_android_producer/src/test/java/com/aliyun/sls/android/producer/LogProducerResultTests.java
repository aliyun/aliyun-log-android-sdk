package com.aliyun.sls.android.producer;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author yulong.gyl
 * @date 2023/8/2
 */
public class LogProducerResultTests {

    @Test
    public void logProducerResult_fromInt() {
        assertThat(LogProducerResult.fromInt(0)).isEqualTo(LogProducerResult.LOG_PRODUCER_OK);
        assertThat(LogProducerResult.fromInt(1)).isEqualTo(LogProducerResult.LOG_PRODUCER_INVALID);
        assertThat(LogProducerResult.fromInt(2)).isEqualTo(LogProducerResult.LOG_PRODUCER_WRITE_ERROR);
        assertThat(LogProducerResult.fromInt(3)).isEqualTo(LogProducerResult.LOG_PRODUCER_DROP_ERROR);
        assertThat(LogProducerResult.fromInt(4)).isEqualTo(LogProducerResult.LOG_PRODUCER_SEND_NETWORK_ERROR);
        assertThat(LogProducerResult.fromInt(5)).isEqualTo(LogProducerResult.LOG_PRODUCER_SEND_QUOTA_ERROR);
        assertThat(LogProducerResult.fromInt(6)).isEqualTo(LogProducerResult.LOG_PRODUCER_SEND_UNAUTHORIZED);
        assertThat(LogProducerResult.fromInt(7)).isEqualTo(LogProducerResult.LOG_PRODUCER_SEND_SERVER_ERROR);
        assertThat(LogProducerResult.fromInt(8)).isEqualTo(LogProducerResult.LOG_PRODUCER_SEND_DISCARD_ERROR);
        assertThat(LogProducerResult.fromInt(9)).isEqualTo(LogProducerResult.LOG_PRODUCER_SEND_TIME_ERROR);
        assertThat(LogProducerResult.fromInt(10)).isEqualTo(LogProducerResult.LOG_PRODUCER_SEND_EXIT_BUFFERED);
        assertThat(LogProducerResult.fromInt(11)).isEqualTo(LogProducerResult.LOG_PRODUCER_PARAMETERS_INVALID);
        assertThat(LogProducerResult.fromInt(99)).isEqualTo(LogProducerResult.LOG_PRODUCER_PERSISTENT_ERROR);
    }

    @Test
    public void logProducerResult_fromInt_withInvalidCode() {
        assertThat(LogProducerResult.fromInt(-1)).isNull();
    }

    @Test
    public void logProducerResult_isLogProducerResultOk() {
        assertThat(LogProducerResult.LOG_PRODUCER_OK.isLogProducerResultOk()).isTrue();
        assertThat(LogProducerResult.LOG_PRODUCER_INVALID.isLogProducerResultOk()).isFalse();
        assertThat(LogProducerResult.LOG_PRODUCER_WRITE_ERROR.isLogProducerResultOk()).isFalse();
        assertThat(LogProducerResult.LOG_PRODUCER_DROP_ERROR.isLogProducerResultOk()).isFalse();
        assertThat(LogProducerResult.LOG_PRODUCER_SEND_NETWORK_ERROR.isLogProducerResultOk()).isFalse();
        assertThat(LogProducerResult.LOG_PRODUCER_SEND_QUOTA_ERROR.isLogProducerResultOk()).isFalse();
        assertThat(LogProducerResult.LOG_PRODUCER_SEND_UNAUTHORIZED.isLogProducerResultOk()).isFalse();
        assertThat(LogProducerResult.LOG_PRODUCER_SEND_SERVER_ERROR.isLogProducerResultOk()).isFalse();
        assertThat(LogProducerResult.LOG_PRODUCER_SEND_DISCARD_ERROR.isLogProducerResultOk()).isFalse();
        assertThat(LogProducerResult.LOG_PRODUCER_SEND_TIME_ERROR.isLogProducerResultOk()).isFalse();
        assertThat(LogProducerResult.LOG_PRODUCER_SEND_EXIT_BUFFERED.isLogProducerResultOk()).isFalse();
        assertThat(LogProducerResult.LOG_PRODUCER_PARAMETERS_INVALID.isLogProducerResultOk()).isFalse();
        assertThat(LogProducerResult.LOG_PRODUCER_PERSISTENT_ERROR.isLogProducerResultOk()).isFalse();
    }
}
