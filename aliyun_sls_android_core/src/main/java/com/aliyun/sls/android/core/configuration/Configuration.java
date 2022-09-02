package com.aliyun.sls.android.core.configuration;

import com.aliyun.sls.android.ot.ISpanProcessor;
import com.aliyun.sls.android.ot.ISpanProvider;

/**
 * @author gordon
 * @date 2022/7/19
 */
public class Configuration {
    private static final String TAG = "Configuration";

    public Boolean enableCrashReporter = Boolean.FALSE;
    public Boolean enableBlockDetection = Boolean.FALSE;
    public Boolean enableNetworkDiagnosis = Boolean.FALSE;

    public String env;
    public final ISpanProcessor spanProcessor;
    public ISpanProvider spanProvider;
    public boolean debuggable = false;

    public UserInfo userInfo;

    public Configuration(ISpanProcessor spanProcessor) {
        this.spanProcessor = spanProcessor;
    }
}
