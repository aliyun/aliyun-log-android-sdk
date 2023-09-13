package com.aliyun.sls.android.otel.common;

/**
 * @author yulong.gyl
 * @date 2023/9/13
 */
public class ConfigurationManager {
    public interface AccessKeyDelegate {
        AccessKeyConfiguration getAccessKey(String scope);
    }

    public interface ResourceDelegate {
        ResourceConfiguration getResource(String scope);
    }

    private AccessKeyDelegate accessKeyDelegate;
    private ResourceDelegate resourceDelegate;

    private static final class Holder {
        private static final ConfigurationManager INSTANCE = new ConfigurationManager();
    }

    private ConfigurationManager() {
        //no instance
    }

    public static ConfigurationManager getInstance() {
        return Holder.INSTANCE;
    }

    public void setDelegate(AccessKeyDelegate accessKeyDelegate, ResourceDelegate resourceDelegate) {
        this.accessKeyDelegate = accessKeyDelegate;
        this.resourceDelegate = resourceDelegate;
    }

    public AccessKeyDelegate getAccessKeyDelegate() {
        return accessKeyDelegate;
    }

    public ResourceDelegate getResourceDelegate() {
        return resourceDelegate;
    }
}
