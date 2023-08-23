package com.aliyun.sls.android.core.configuration;

import java.util.LinkedHashMap;
import java.util.Map;

import android.text.TextUtils;

/**
 * @author gordon
 * @date 2022/7/19
 */
public class UserInfo {
    public String uid;
    public String channel;
    public final Map<String, String> ext = new LinkedHashMap<>();

    public void addExt(String key, String value) {
        if (TextUtils.isEmpty(key)) {
            return;
        }

        ext.put(key, value);
    }

}
