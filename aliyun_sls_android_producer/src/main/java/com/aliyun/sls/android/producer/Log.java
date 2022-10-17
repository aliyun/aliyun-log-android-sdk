package com.aliyun.sls.android.producer;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import android.text.TextUtils;
import com.aliyun.sls.android.producer.utils.TimeUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Log {

    private long logTime;

    private final Map<String, String> content = new LinkedHashMap<>();
    private final Object lock = new Object();

    public Log() {
        this.logTime = TimeUtils.getTimeInMillis();
    }

    public void putContents(Map<String, String> contents) {
        if (null == contents || contents.isEmpty()) {
            return;
        }

        synchronized (lock) {
            content.putAll(new LinkedHashMap<String, String>(contents));
        }
    }

    public void putContent(String key, String value) {
        synchronized (lock) {
            content.put(key, value);
        }
    }

    public void putContent(String key, int value) {
        synchronized (lock) {
            content.put(key, String.valueOf(value));
        }
    }

    public void putContent(String key, long value) {
        synchronized (lock) {
            content.put(key, String.valueOf(value));
        }
    }

    public void putContent(String key, boolean value) {
        synchronized (lock) {
            content.put(key, String.valueOf(value));
        }
    }

    public void putContent(String key, float value) {
        synchronized (lock) {
            content.put(key, String.valueOf(value));
        }
    }

    public void putContent(String key, double value) {
        synchronized (lock) {
            content.put(key, String.valueOf(value));
        }
    }

    public void putContent(String key, JSONObject object) {
        synchronized (lock) {
            if (null != object) {
                content.put(key, object.toString());
            } else {
                content.put(key, "null");
            }
        }
    }

    public void putContent(String key, JSONArray array) {
        synchronized (lock) {
            if (null != array) {
                content.put(key, array.toString());
            } else {
                content.put(key, "null");
            }
        }
    }

    public void putContent(JSONObject jsonObject) {
        if (null == jsonObject || jsonObject.length() == 0) {
            return;
        }

        JSONObject tmp;
        try {
            tmp = new JSONObject(jsonObject.toString());
        } catch (JSONException e) {
            tmp = null;
        }

        if (null == tmp) {
            return;
        }

        Iterator<String> it = tmp.keys();
        String key;
        Object value;
        Map<String, String> kvMap = new LinkedHashMap<>();
        while (it.hasNext()) {
            key = it.next();
            if (TextUtils.isEmpty(key)) {
                continue;
            }

            value = tmp.opt(key);
            if (null == value || JSONObject.NULL == value || value instanceof Boolean) {
                kvMap.put(key, String.valueOf(value));
                continue;
            }

            if (value instanceof JSONObject) {
                kvMap.put(key, ((JSONObject)value).toString());
                continue;
            }

            if (value instanceof JSONArray) {
                kvMap.put(key, ((JSONArray)value).toString());
                continue;
            }

            if (value instanceof Number) {
                kvMap.put(key, numberToString((Number)value));
                continue;
            }

            kvMap.put(key, value.toString());
        }

        this.putContents(kvMap);
    }

    private String numberToString(Number value) {
        try {
            return JSONObject.numberToString(value);
        } catch (JSONException e) {
            return "";
        }
    }

    public Map<String, String> getContent() {
        synchronized (lock) {
            return content;
        }
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
