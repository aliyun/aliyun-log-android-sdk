package com.aliyun.sls.android.producer;

import com.aliyun.sls.android.producer.utils.TimeUtils;

import java.util.HashMap;
import java.util.Map;

public class Log {

    private long logTime;

    private Map<String, String> content = new HashMap<>();

    public Log() {
        this.logTime = TimeUtils.getTimeInMillis();
    }

    public void putContents(Map<String, String> contents) {
        if (null == contents || contents.isEmpty()) {
            return;
        }

        content.putAll(contents);
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

    /**
     * This method will be removed in future version.
     * Set the __time__ field. Do not call this method for set __time__ field, this may case data lost.
     *
     * @param logTime time in second
     */
    @Deprecated
    public void setLogTime(long logTime) {
        this.logTime = logTime;
    }
}
