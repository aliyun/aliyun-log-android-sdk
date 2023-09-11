package com.aliyun.sls.android.core.configuration;

/**
 * @author yulong.gyl
 * @date 2023/9/11
 */
public final class ConfigurationManager {
    private static AccessKeyDelegate accessKeyDelegate;
    private static ResourceDelegate resourceDelegate;

    private ConfigurationManager() {
        //no instance
    }

    public static void setAccessKeyDelegate(AccessKeyDelegate delegate) {
        accessKeyDelegate = delegate;
    }

    public static AccessKeyDelegate getAccessKeyDelegate() {
        return accessKeyDelegate;
    }

    public static ResourceDelegate getResourceDelegate() {
        return resourceDelegate;
    }

    public static void setResourceDelegate(ResourceDelegate resourceDelegate) {
        ConfigurationManager.resourceDelegate = resourceDelegate;
    }
}
