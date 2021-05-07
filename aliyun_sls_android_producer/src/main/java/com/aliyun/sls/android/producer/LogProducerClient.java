package com.aliyun.sls.android.producer;

import java.util.Map;

import com.aliyun.sls.android.producer.profiler.SLSProfiler;

public class LogProducerClient {

    private final LogProducerConfig logProducerConfig;
    private final long producer;
    private final long client;

    public LogProducerClient(LogProducerConfig logProducerConfig) throws LogProducerException {
        this(logProducerConfig, null);
    }

    public LogProducerClient(LogProducerConfig logProducerConfig, final LogProducerCallback callback) throws LogProducerException {
        this.logProducerConfig = logProducerConfig;

        if (logProducerConfig.isEnableProfiler()) {
            producer = create_log_producer(logProducerConfig.getConfig(), new LogProducerCallback() {
                @Override
                public void onCall(int resultCode, String reqId, String errorMessage, int logBytes, int compressedBytes) {
                    final LogProducerResult result = LogProducerResult.fromInt(resultCode);
                    SLSProfiler.getInstance().reportSendLog(result.isLogProducerResultOk(), result);

                    if (null != callback) {
                        callback.onCall(resultCode, reqId, errorMessage, logBytes, compressedBytes);
                    }
                }
            });
        } else {
            producer = create_log_producer(logProducerConfig.getConfig(), callback);
        }

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
        long logTime = log.getLogTime();
        int res = log_producer_client_add_log_with_len(client, logTime, pairCount, keyArray, valueArray, flush);
        final LogProducerResult result = LogProducerResult.fromInt(res);

        if (logProducerConfig.isEnableProfiler()) {
            SLSProfiler.getInstance().reportAddLog(result.isLogProducerResultOk(), result);
        }

        return result;
    }

    public void destroyLogProducer() {
        destroy_log_producer(producer);
    }

    private static native long create_log_producer(long config, LogProducerCallback callback);

    private static native long get_log_producer_client(long producer);

    private static native int log_producer_client_add_log_with_len(long config, long log_time, int pairCount, String[] keys, String[] values, int flush);

    private static native void destroy_log_producer(long producer);

}
