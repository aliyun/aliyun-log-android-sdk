package com.aliyun.sls.android.core.configuration;

import com.aliyun.sls.android.ot.ISpanProcessor;
import com.aliyun.sls.android.ot.ISpanProvider;

/**
 * @author gordon
 * @date 2022/7/19
 */
public class Configuration {
    private static final String TAG = "Configuration";

    public Boolean enableCrashReporter = Boolean.TRUE;

    public String env;
    public final ISpanProcessor spanProcessor;
    public ISpanProvider spanProvider;

    public UserInfo userInfo;

    public Configuration(ISpanProcessor spanProcessor) {
        this.spanProcessor = spanProcessor;
    }
}
