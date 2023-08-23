package com.aliyun.sls.android.core.sender;

import com.aliyun.sls.android.core.configuration.Credentials;
import com.aliyun.sls.android.producer.Log;

/**
 * @author gordon
 * @date 2022/7/19
 */
public class NoOpSender implements Sender {
    protected Callback callback;

    @Override
    public void initialize(Credentials credentials) {

    }

    @Override
    public boolean send(Log data) {
        return false;
    }

    @Override
    public void setCredentials(Credentials credentials) {

    }

    @Override
    public void setCallback(Callback callback) {
        this.callback = callback;
    }
}
