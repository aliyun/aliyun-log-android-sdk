package com.aliyun.sls.android.plugin;

import com.aliyun.sls.android.SLSConfig;

/**
 * @author gordon
 * @date 2021/04/14
 */
public abstract class AbstractPlugin implements IPlugin {
    protected boolean debuggable = false;

    @Override
    public void setDebuggable(boolean debuggable) {
        this.debuggable = debuggable;
    }

    @Override
    public void resetSecurityToken(String accessKeyId, String accessKeySecret, String securityToken) {

    }

    @Override
    public void resetProject(String endpoint, String project, String logstore) {

    }

    @Override
    public void updateConfig(SLSConfig config) {

    }
}
