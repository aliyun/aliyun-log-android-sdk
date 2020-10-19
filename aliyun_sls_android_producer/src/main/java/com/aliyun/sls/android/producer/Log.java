package com.aliyun.sls.android.producer;

import java.util.HashMap;
import java.util.Map;

public class Log {
    private Map<String, String> content = new HashMap<>();

    public void putContent(String key, String value) {
        content.put(key, value);
    }

    public Map<String, String> getContent() {
        return content;
    }
}
