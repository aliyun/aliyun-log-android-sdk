package com.aliyun.sls.android.producer;

import java.util.Map;

public class LogProducerClient {

    private long producer;
    private long client;

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
        if(client == 0) {
            return LogProducerResult.LOG_PRODUCER_INVALID;
        }
        Map<String, String> contents = log.getContent();
        int pairCount = contents.size();

        String[] keyArray = new String[pairCount];
        String[] valueArray = new String[pairCount];

        int[] keyCountArray = new int[pairCount];
        int[] valueCountArray = new int[pairCount];

        int i = 0;
        for (Map.Entry<String, String> entry : contents.entrySet()) {
            String key = entry.getKey();
            keyArray[i] = key;
            keyCountArray[i] = key.getBytes().length;

            String value = entry.getValue();
            valueArray[i] = value;

            valueCountArray[i] = value.getBytes().length;

            i++;
        }
        int res = log_producer_client_add_log_with_len(client, pairCount, keyArray, keyCountArray, valueArray, valueCountArray, flush);
        return LogProducerResult.fromInt(res);
    }

    public void destroyLogProducer() {
        destroy_log_producer(producer);
    }

    private static native long create_log_producer(long config, LogProducerCallback callback);

    private static native long get_log_producer_client(long producer);

    private static native int log_producer_client_add_log_with_len(long config, int pairCount, String[] keys, int[] keyLens, String[] values, int[] valueLens, int flush);

    private static native void destroy_log_producer(long producer);

    private static native void log_producer_env_destroy();
}
