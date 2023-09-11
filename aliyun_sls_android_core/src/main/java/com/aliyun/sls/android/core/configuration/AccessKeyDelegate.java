package com.aliyun.sls.android.core.configuration;

/**
 * @author yulong.gyl
 * @date 2023/9/11
 */
public interface AccessKeyDelegate {
    String getAccessKeyId(String scope);

    String getAccessKeySecret(String scope);

    String getAccessKeyToken(String scope);
}
