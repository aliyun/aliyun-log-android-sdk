package com.aliyun.sls.android.producer;

import java.util.Map;

import android.content.Context;
import com.aliyun.sls.android.producer.utils.TimeUtils;
import com.aliyun.sls.android.scheme.Scheme;

public class LogProducerClient {

    public interface IAddLogInterceptor {
        void onBeforeLogAdded(Log log);
    }

    private final LogProducerConfig config;
    private final long producer;
    private final long client;

    @Deprecated
    private IAddLogInterceptor addLogInterceptor;

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

        TimeUtils.startUpdateServerTime(logProducerConfig.getContext(), logProducerConfig.getEndpoint(), logProducerConfig.getProject());
    }

    public LogProducerResult addLog(Log log) {
        return addLog(log, 0);
    }

    public LogProducerResult addLog(Log log, int flush) {
        if (client == 0 || log == null) {
            return LogProducerResult.LOG_PRODUCER_INVALID;
        }

        if (config.isEnableTrack()) {
            final Context context = config.getContext();
            if (null != context) {
                Scheme scheme = Scheme.createDefaultScheme(config.getContext());
                for (Map.Entry<String, String> entry : scheme.toMap().entrySet()) {
                    log.putContent(entry.getKey(), entry.getValue());
                }
            }
        } else {
            if (null != addLogInterceptor) {
                addLogInterceptor.onBeforeLogAdded(log);
            }
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
        return LogProducerResult.fromInt(res);
    }

    @Deprecated
    public void setAddLogInterceptor(IAddLogInterceptor addLogInterceptor) {
        this.addLogInterceptor = addLogInterceptor;
    }

    public LogProducerResult addLogRaw(byte[][] keys, byte[][] values) {
        if (client == 0 || null == keys || null == values ) {
            return LogProducerResult.LOG_PRODUCER_INVALID;
        }

        long logTime = new Log().getLogTime();
        int res = log_producer_client_add_log_with_len_time_int32(client, logTime, keys.length, keys, values);
        return LogProducerResult.fromInt(res);
    }

    public void destroyLogProducer() {
        destroy_log_producer(producer);
    }

    private static native long create_log_producer(long config, LogProducerCallback callback);

    private static native long get_log_producer_client(long producer);

    private static native int log_producer_client_add_log_with_len(long config, long log_time, int pairCount,
        String[] keys, String[] values, int flush);

    private static native int log_producer_client_add_log_with_len_time_int32(long config, long log_time, int pairCount, byte[][] keys, byte[][] values);

    private static native void destroy_log_producer(long producer);

}
