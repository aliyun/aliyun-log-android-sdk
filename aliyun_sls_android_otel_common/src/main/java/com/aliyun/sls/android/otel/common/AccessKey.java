package com.aliyun.sls.android.otel.common;

/**
 * @author yulong.gyl
 * @date 2023/9/13
 */
public class AccessKey {
    private final String accessKeyId;
    private final String accessKeySecret;
    private final String accessKeySecurityToken;

    private AccessKey(String accessKeyId, String accessKeySecret, String accessKeySecurityToken) {
        this.accessKeyId = accessKeyId;
        this.accessKeySecret = accessKeySecret;
        this.accessKeySecurityToken = accessKeySecurityToken;
    }

    public static AccessKey accessKey(String accessKeyId, String accessKeySecret,
        String accessKeySecurityToken) {
        return new AccessKey(accessKeyId, accessKeySecret, accessKeySecurityToken);
    }

    public String getAccessKeyId() {
        return accessKeyId;
    }

    public String getAccessKeySecret() {
        return accessKeySecret;
    }

    public String getAccessKeySecurityToken() {
        return accessKeySecurityToken;
    }
}
