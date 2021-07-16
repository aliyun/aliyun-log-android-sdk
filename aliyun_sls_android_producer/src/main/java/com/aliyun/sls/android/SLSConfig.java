package com.aliyun.sls.android;

import java.util.LinkedHashMap;
import java.util.Map;

import android.content.Context;
import com.aliyun.sls.android.scheme.TCUtils;

/**
 * @author gordon
 * @date 2021/04/14
 */
public final class SLSConfig {

    public boolean debuggable = false;
    public String appVersion = "--";
    public String appName = "--";

    public Context context;

    public String endpoint;
    public String accessKeyId;
    public String accessKeySecret;
    public String securityToken;

    public String pluginAppId;
    public String pluginLogproject;

    public String channel;
    public String channelName;
    public String userNick;
    public String longLoginNick;
    public String userId;
    public String longLoginUserId;
    public String loginType;

    private Map<String, String> ext = new LinkedHashMap<>();

    public SLSConfig(Context context) {
        this.context = context;
        this.appVersion = TCUtils.getAppVersion(context);
        this.appName = TCUtils.getAppName(context);
    }

    public void addCustom(String key, String value) {
        if (null == key) {
            key = "null";
        }
        if(null == value) {
            value = "null";
        }

        ext.put(key, value);
    }

    public Map<String, String> getExt() {
        return ext;
    }
}
