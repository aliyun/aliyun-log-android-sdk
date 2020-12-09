package com.aliyun.sls.android.producer;

public enum LogProducerResult {

    LOG_PRODUCER_OK(0),
    LOG_PRODUCER_INVALID(1),
    LOG_PRODUCER_WRITE_ERROR(2),
    LOG_PRODUCER_DROP_ERROR(3),
    LOG_PRODUCER_SEND_NETWORK_ERROR(4),
    LOG_PRODUCER_SEND_QUOTA_ERROR(5),
    LOG_PRODUCER_SEND_UNAUTHORIZED(6),
    LOG_PRODUCER_SEND_SERVER_ERROR(7),
    LOG_PRODUCER_SEND_DISCARD_ERROR(8),
    LOG_PRODUCER_SEND_TIME_ERROR(9),
    LOG_PRODUCER_SEND_EXIT_BUFFERED(10),
    LOG_PRODUCER_PERSISTENT_ERROR(99);


    private final int code;

    LogProducerResult(int code) {
        this.code = code;
    }

    public static LogProducerResult fromInt(int code) {
        for (LogProducerResult timeSpanType : LogProducerResult.values()) {
            if (timeSpanType.code == code) {
                return timeSpanType;
            }
        }
        return null;
    }

    public boolean isLogProducerResultOk() {
        return this.code == LOG_PRODUCER_OK.code;
    }
}
