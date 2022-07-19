package com.aliyun.sls.android.core.configuration;

/**
 * @author gordon
 * @date 2022/7/19
 */
public class Credentials {
    public String instanceId;
    public Endpoint endpoint;
    public String project;

    public String accessKeyId;
    public String accessKeySecret;
    public String securityToken;

    public static class Endpoint {
        public final String endpoint;

        Endpoint(String endpoint) {
            this.endpoint = endpoint;
        }

        public static Endpoint of(String endpoint) {
            return new Endpoint(endpoint);
        }
    }

}
