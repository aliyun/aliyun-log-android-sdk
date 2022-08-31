package com.aliyun.sls.android.core.feature;

import java.util.Map;

import android.content.Context;
import com.aliyun.sls.android.core.configuration.Configuration;
import com.aliyun.sls.android.core.configuration.Credentials;
import com.aliyun.sls.android.core.sender.Sender.Callback;
import com.aliyun.sls.android.producer.LogProducerResult;

/**
 * @author gordon
 * @date 2022/7/19
 */
public class NoOpFeature implements Feature {
    protected Callback callback;
    @Override
    public String name() {
        return "";
    }

    @Override
    public String version() {
        return "";
    }

    @Override
    public void initialize(Context context, Credentials credentials, Configuration configuration) {

    }

    @Override
    public boolean isInitialize() {
        return false;
    }

    @Override
    public void stop() {

    }

    @Override
    public void setCredentials(Credentials credentials) {

    }

    @Override
    public void setCallback(Callback callback) {
        this.callback = callback;
    }

    @Override
    public void setFeatureEnabled(boolean enable) {

    }

    @Override
    public boolean isFeatureEnabled() {
        return true;
    }
}
