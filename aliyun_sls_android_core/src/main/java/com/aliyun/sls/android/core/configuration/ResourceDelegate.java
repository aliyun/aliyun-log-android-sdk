package com.aliyun.sls.android.core.configuration;

/**
 * @author yulong.gyl
 * @date 2023/9/11
 */
public interface ResourceDelegate {
    String getEndpoint(String scope);

    String getProject(String scope);

    String getInstanceId(String scope);
}
