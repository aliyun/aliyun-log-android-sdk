package com.aliyun.sls.android.otel.common;

/**
 * @author yulong.gyl
 * @date 2023/9/13
 */
public class ConfigurationManager {
    public interface AccessKeyDelegate {
        AccessKey getAccessKey(String scope);
    }

    public interface ResourceDelegate {
        Resource getResource(String scope);
    }

    public interface ConfigurationDelegate {
        Configuration getConfiguration(String scope);
    }

    private AccessKeyDelegate accessKeyDelegate;
    private ResourceDelegate resourceDelegate;
    private ConfigurationDelegate configurationDelegate;

    private static final class Holder {
        private static final ConfigurationManager INSTANCE = new ConfigurationManager();
    }

    private ConfigurationManager() {
        //no instance
    }

    public static ConfigurationManager getInstance() {
        return Holder.INSTANCE;
    }

    public void setDelegate(AccessKeyDelegate accessKeyDelegate, ResourceDelegate resourceDelegate,
        ConfigurationDelegate configurationDelegate) {
        this.accessKeyDelegate = accessKeyDelegate;
        this.resourceDelegate = resourceDelegate;
        this.configurationDelegate = configurationDelegate;
    }

    public AccessKeyDelegate getAccessKeyDelegate() {
        return accessKeyDelegate;
    }

    public ResourceDelegate getResourceDelegate() {
        return resourceDelegate;
    }

    public ConfigurationDelegate getConfigurationDelegate() {
        return configurationDelegate;
    }
}
