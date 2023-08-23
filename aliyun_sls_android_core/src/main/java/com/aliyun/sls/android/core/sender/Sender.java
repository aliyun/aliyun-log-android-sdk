package com.aliyun.sls.android.core.sender;

import com.aliyun.sls.android.core.configuration.Credentials;
import com.aliyun.sls.android.producer.Log;
import com.aliyun.sls.android.producer.LogProducerResult;

/**
 * @author gordon
 * @date 2021/08/26
 */
public interface Sender {
    interface Callback {
        void onCall(String feature, LogProducerResult result);
    }

    void initialize(Credentials credentials);

    /**
     * send report data to remote server.
     */
    boolean send(Log data);

    void setCredentials(Credentials credentials);

    void setCallback(Callback callback);

}
