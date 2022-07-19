package com.aliyun.sls.android.core.feature;

import java.util.Map;

import android.content.Context;
import com.aliyun.sls.android.core.configuration.Configuration;
import com.aliyun.sls.android.core.configuration.Credentials;

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

    void updateCredentials(Credentials credentials);

    void addCustom(final String eventId, final Map<String, String> properties);
}
