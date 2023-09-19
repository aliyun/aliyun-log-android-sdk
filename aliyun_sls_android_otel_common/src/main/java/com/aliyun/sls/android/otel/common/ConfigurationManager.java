package com.aliyun.sls.android.otel.common;

/**
 * @author yulong.gyl
 * @date 2023/9/13
 */
public class ConfigurationManager {
    public interface AccessKeyProvider {
        AccessKey getAccessKey(String scope);
    }

    public interface WorkspaceProvider {
        Workspace getResource(String scope);
    }

    public interface EnvironmentProvider {
        Environment getEnvironment(String scope);
    }

    private AccessKeyProvider accessKeyProvider;
    private WorkspaceProvider workspaceProvider;
    private EnvironmentProvider environmentProvider;

    private static final class Holder {
        private static final ConfigurationManager INSTANCE = new ConfigurationManager();
    }

    private ConfigurationManager() {
        //no instance
    }

    public static ConfigurationManager getInstance() {
        return Holder.INSTANCE;
    }

    public void setProvider(AccessKeyProvider accessKeyProvider, WorkspaceProvider workspaceProvider,
        EnvironmentProvider environmentProvider) {
        this.accessKeyProvider = accessKeyProvider;
        this.workspaceProvider = workspaceProvider;
        this.environmentProvider = environmentProvider;
    }

    public AccessKeyProvider getAccessKeyProvider() {
        return accessKeyProvider;
    }

    public WorkspaceProvider getWorkspaceProvider() {
        return workspaceProvider;
    }

    public EnvironmentProvider getEnvironmentProvider() {
        return environmentProvider;
    }
}
