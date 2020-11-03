package com.aliyun.sls.android.producer;

import android.support.v4.util.ArrayMap;
import java.util.Map;

public class Log {
    private Map<String, String> content = new ArrayMap<>();

    public void putContent(String key, String value) {
        content.put(key, value);
    }

    public Map<String, String> getContent() {
        return content;
    }
}
