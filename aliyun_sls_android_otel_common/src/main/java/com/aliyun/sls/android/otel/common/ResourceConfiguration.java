package com.aliyun.sls.android.otel.common;

/**
 * @author yulong.gyl
 * @date 2023/9/13
 */
public class ResourceConfiguration {
    private String endpoint;
    private String project;
    private String instanceId;

    private ResourceConfiguration(String endpoint, String project, String instanceId) {
        this.endpoint = endpoint;
        this.project = project;
        this.instanceId = instanceId;
    }

    public static ResourceConfiguration configuration(String endpoint, String project, String instanceId) {
        return new ResourceConfiguration(endpoint, project, instanceId);
    }

    public String getEndpoint() {
        return endpoint;
    }

    public String getProject() {
        return project;
    }

    public String getInstanceId() {
        return instanceId;
    }
}
