package com.aliyun.sls.android.producer;

import java.util.HashMap;
import java.util.Map;

import com.aliyun.sls.android.producer.utils.TimeUtils;

public class Log {

    private long logTime;

    private Map<String, String> content = new HashMap<>();

    public Log() {
        this.logTime = TimeUtils.getTimeInMillis();
    }

    public void putContent(String key, String value) {
        content.put(key, value);
    }

    public Map<String, String> getContent() {
        return content;
    }

    public long getLogTime() {
        return logTime;
    }

    public void setLogTime(long logTime) {
        this.logTime = logTime;
    }
}
