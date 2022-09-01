package com.aliyun.sls.android.core.feature;

import android.content.Context;
import com.aliyun.sls.android.core.configuration.Configuration;
import com.aliyun.sls.android.core.configuration.Credentials;
import com.aliyun.sls.android.core.sender.Sender.Callback;

/**
 * @author gordon
 * @date 2021/04/14
 */
public interface Feature {

    String name();

    String version();

    void initialize(Context context, Credentials credentials, Configuration configuration);

    boolean isInitialize();

    void stop();

    void setCredentials(Credentials credentials);

    void setCallback(Callback callback);

    void setFeatureEnabled(boolean enable);

    boolean isFeatureEnabled();
}
