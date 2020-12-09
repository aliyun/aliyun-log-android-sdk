package com.aliyun.sls.android.producer;

import java.util.Map;

public class LogProducerClient {

    private final long producer;
    private final long client;

    public LogProducerClient(LogProducerConfig logProducerConfig) throws LogProducerException {
        this(logProducerConfig, null);
    }

    public LogProducerClient(LogProducerConfig logProducerConfig, LogProducerCallback callback) throws LogProducerException {
        producer = create_log_producer(logProducerConfig.getConfig(), callback);
        if (producer == 0) {
            throw new LogProducerException("Can not create log producer");
        }
        client = get_log_producer_client(producer);
        if (client == 0) {
            throw new LogProducerException("Can not create log producer client");
        }
    }

    public LogProducerResult addLog(Log log) {
        return addLog(log, 0);
    }

    public LogProducerResult addLog(Log log, int flush) {
        if (client == 0 || log == null) {
            return LogProducerResult.LOG_PRODUCER_INVALID;
        }
        Map<String, String> contents = log.getContent();
        int pairCount = contents.size();

        String[] keyArray = new String[pairCount];
        String[] valueArray = new String[pairCount];

        int i = 0;
        for (Map.Entry<String, String> entry : contents.entrySet()) {
            String key = entry.getKey();
            key = key == null ? "" : key;
            keyArray[i] = key;

            String value = entry.getValue();
            value = value == null ? "" : value;
            valueArray[i] = value;

            i++;
        }
        int res = log_producer_client_add_log_with_len(client, pairCount, keyArray, valueArray, flush);
        return LogProducerResult.fromInt(res);
    }

    public void destroyLogProducer() {
        destroy_log_producer(producer);
    }

    private static native long create_log_producer(long config, LogProducerCallback callback);

    private static native long get_log_producer_client(long producer);

    private static native int log_producer_client_add_log_with_len(long config, int pairCount, String[] keys, String[] values, int flush);

    private static native void destroy_log_producer(long producer);

}
