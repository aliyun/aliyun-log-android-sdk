package com.aliyun.sls.android.producer;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static org.junit.Assert.*;
/**
 * @author gordon
 * @date 2023/4/4
 */
@RunWith(JUnit4.class)
public class LogProducerResultTest {

    @Test
    public void result_Test() {
        assertEquals(13, LogProducerResult.values().length);

        assertEquals(LogProducerResult.LOG_PRODUCER_OK, LogProducerResult.fromInt(0));
        assertEquals(LogProducerResult.LOG_PRODUCER_INVALID, LogProducerResult.fromInt(1));
        assertEquals(LogProducerResult.LOG_PRODUCER_WRITE_ERROR, LogProducerResult.fromInt(2));
        assertEquals(LogProducerResult.LOG_PRODUCER_DROP_ERROR, LogProducerResult.fromInt(3));
        assertEquals(LogProducerResult.LOG_PRODUCER_SEND_NETWORK_ERROR, LogProducerResult.fromInt(4));
        assertEquals(LogProducerResult.LOG_PRODUCER_SEND_QUOTA_ERROR, LogProducerResult.fromInt(5));
        assertEquals(LogProducerResult.LOG_PRODUCER_SEND_UNAUTHORIZED, LogProducerResult.fromInt(6));
        assertEquals(LogProducerResult.LOG_PRODUCER_SEND_SERVER_ERROR, LogProducerResult.fromInt(7));
        assertEquals(LogProducerResult.LOG_PRODUCER_SEND_DISCARD_ERROR, LogProducerResult.fromInt(8));
        assertEquals(LogProducerResult.LOG_PRODUCER_SEND_TIME_ERROR, LogProducerResult.fromInt(9));
        assertEquals(LogProducerResult.LOG_PRODUCER_SEND_EXIT_BUFFERED, LogProducerResult.fromInt(10));
        assertEquals(LogProducerResult.LOG_PRODUCER_PARAMETERS_INVALID, LogProducerResult.fromInt(11));
        assertEquals(LogProducerResult.LOG_PRODUCER_PERSISTENT_ERROR, LogProducerResult.fromInt(99));
    }
}
