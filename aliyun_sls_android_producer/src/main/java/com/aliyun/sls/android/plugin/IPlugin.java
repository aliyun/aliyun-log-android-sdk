package com.aliyun.sls.android.plugin;

import com.aliyun.sls.android.SLSConfig;

/**
 * @author gordon
 * @date 2021/04/14
 */
public interface IPlugin {

    String name();

    String version();

    void init(SLSConfig config);

    void setDebuggable(boolean debuggable);

    void resetSecurityToken(String accessKeyId, String accessKeySecret, String securityToken);

    void resetProject(String endpoint, String project, String logstore);

    void updateConfig(SLSConfig config);
}
