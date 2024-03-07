package com.aliyun.sls.android.crashreporter.parser;

import java.util.HashMap;
import java.util.Map;

/**
 * @author gordon
 * @date 2022/5/9
 */
class KeyConverter {
    private static final Map<String, String> KEYS = new HashMap<>();

    static {
        // common
        KEYS.put("meminfo:", "mem_status");
        KEYS.put("status:", "process_status");
        KEYS.put("memory info:", "memory");
        KEYS.put("recent status:", "recent_status");
        // jni crash
        KEYS.put("all threads dump:", "java_stacktrace");
        // anr
        KEYS.put("anr traces:", "stacktrace");
    }

    static String convert(String origin) {
        if (KEYS.containsKey(origin)) {
            return KEYS.get(origin);
        }

        if (origin.lastIndexOf(":") != -1) {
            origin = origin.substring(0, origin.length() - 1);
        }
        if (origin.contains(" ")) {
            origin = origin.replaceAll(" ", "_");
        }
        return origin;
    }
}
