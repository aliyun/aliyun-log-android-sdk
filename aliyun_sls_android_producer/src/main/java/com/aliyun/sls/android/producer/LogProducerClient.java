package com.aliyun.sls.android.producer;

import java.util.LinkedHashMap;
import java.util.Map;

import android.text.TextUtils;
import com.aliyun.sls.android.producer.utils.ThreadUtils;
import com.aliyun.sls.android.producer.utils.TimeUtils;

public class LogProducerClient {
    private final LogProducerConfig config;
    private final long producer;
    private final long client;

    private boolean enable;

    public LogProducerClient(LogProducerConfig logProducerConfig) throws LogProducerException {
        this(logProducerConfig, null);
    }

    public LogProducerClient(LogProducerConfig logProducerConfig, LogProducerCallback callback)
        throws LogProducerException {
        this.config = logProducerConfig;
        producer = create_log_producer(logProducerConfig.getConfig(), callback);
        if (producer == 0) {
            throw new LogProducerException("Can not create log producer");
        }
        client = get_log_producer_client(producer);
        if (client == 0) {
            throw new LogProducerException("Can not create log producer client");
        }

        final String endpoint = logProducerConfig.getEndpoint();
        final String project = logProducerConfig.getProject();
        if (!TextUtils.isEmpty(endpoint) && !TextUtils.isEmpty(project)) {
            TimeUtils.startUpdateServerTime(logProducerConfig.getContext(), endpoint, project);
        }
        enable = true;
    }

    public LogProducerResult addLog(Log log) {
        return addLog(log, 0);
    }

    public LogProducerResult addLog(Log log, int flush) {
        if (!enable || client == 0 || log == null) {
            return LogProducerResult.LOG_PRODUCER_INVALID;
        }

        Map<String, String> contents = new LinkedHashMap<>(log.getContent());
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
        long logTime = log.getLogTime();
        int res = log_producer_client_add_log_with_len(client, logTime, pairCount, keyArray, valueArray, flush);
        return LogProducerResult.fromInt(res);
    }

    public LogProducerResult addLogRaw(byte[][] keys, byte[][] values) {
        if (!enable || client == 0 || null == keys || null == values ) {
            return LogProducerResult.LOG_PRODUCER_INVALID;
        }

        long logTime = new Log().getLogTime();
        int res = log_producer_client_add_log_with_len_time_int32(client, logTime, keys.length, keys, values);
        return LogProducerResult.fromInt(res);
    }

    public void destroyLogProducer() {
        if (!enable) {
            return;
        }
        enable = false;
        // destroy method will send data in mem. in case of anr, this should work on sub thread.
        ThreadUtils.exec(new Runnable() {
            @Override
            public void run() {
                destroy_log_producer(producer);
            }
        });
    }

    private static native long create_log_producer(long config, LogProducerCallback callback);

    private static native long get_log_producer_client(long producer);

    private static native int log_producer_client_add_log_with_len(long config, long log_time, int pairCount,
        String[] keys, String[] values, int flush);

    private static native int log_producer_client_add_log_with_len_time_int32(long config, long log_time, int pairCount, byte[][] keys, byte[][] values);

    private static native void destroy_log_producer(long producer);

}
