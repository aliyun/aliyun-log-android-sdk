package com.aliyun.sls.android.producer;

public enum LogProducerResult {

    /**
     * 成功。
     */
    LOG_PRODUCER_OK(0),
    /**
     * Client 已销毁或无效。
     */
    LOG_PRODUCER_INVALID(1),
    /**
     * 数据写入错误。
     */
    LOG_PRODUCER_WRITE_ERROR(2),
    /**
     * 缓存已满。
     */
    LOG_PRODUCER_DROP_ERROR(3),
    /**
     * 网络错误。
     */
    LOG_PRODUCER_SEND_NETWORK_ERROR(4),
    /**
     * shard 已满，需要 shard 扩容。
     */
    LOG_PRODUCER_SEND_QUOTA_ERROR(5),
    /**
     * 授权过期。
     */
    LOG_PRODUCER_SEND_UNAUTHORIZED(6),
    /**
     * 服务错误。
     */
    LOG_PRODUCER_SEND_SERVER_ERROR(7),
    /**
     * 数据被丢弃。
     */
    LOG_PRODUCER_SEND_DISCARD_ERROR(8),
    /**
     * 与服务器时间不同步。
     */
    LOG_PRODUCER_SEND_TIME_ERROR(9),
    /**
     * Client 退出时，缓存数据还没发出。
     */
    LOG_PRODUCER_SEND_EXIT_BUFFERED(10),
    /**
     * Client 初始化参数为空或不正确。
     */
    LOG_PRODUCER_PARAMETERS_INVALID(11),
    /**
     * 缓存数据写入磁盘失败。
     */
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
