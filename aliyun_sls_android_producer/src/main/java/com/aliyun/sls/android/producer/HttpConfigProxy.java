package com.aliyun.sls.android.producer;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * @author gordon
 * @date 2021/06/24
 */
public final class HttpConfigProxy {
    private static final Map<String, String> PLUGIN_USER_AGENTS = new LinkedHashMap<>();

    static {
        addPluginUserAgent("sls-android-sdk", BuildConfig.VERSION_NAME);
    }

    private HttpConfigProxy() {
        //no instance
    }

    public static void addPluginUserAgent(String plugin, String version) {
        PLUGIN_USER_AGENTS.put(plugin, version);
    }

    public static String getUserAgent() {
        StringBuilder builder = new StringBuilder();
        for (Entry<String, String> entry : PLUGIN_USER_AGENTS.entrySet()) {
            builder.append(entry.getKey());
            builder.append("/");
            builder.append(entry.getValue());
            builder.append(";");
        }

        return builder.substring(0, builder.length() - 1);
    }
}
