package com.aliyun.sls.android.crashreporter.parser;

import androidx.annotation.NonNull;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author gordon
 * @date 2022/5/9
 */
class LogParserResult extends JSONObject {
    public LogParserResult() {
        super();
    }

    public void putObject(String key, Object value) {
        try {
            super.putOpt(key, value);
        } catch (JSONException e) {
            // ignore
        }
    }

    public void put(String key, LogParserResult value) {
        try {
            super.put(key, (Object)value);
        } catch (JSONException e) {
            // ignore
        }
    }

    @Override
    public Object get(String key) {
        try {
            return super.get(key);
        } catch (Throwable t) {
            return null;
        }
    }

    @Override
    public boolean getBoolean(@NonNull String name) {
        try {
            return super.getBoolean(name);
        } catch (Throwable t) {
            return false;
        }

    }

    @Override
    public int getInt(@NonNull String name) {
        try {
            return super.getInt(name);
        } catch (Throwable t) {
            return 0;
        }
    }

    @NonNull
    @Override
    public String getString(@NonNull String name) {
        try {
            return super.getString(name);
        } catch (Throwable t) {
            return null;
        }
    }
}
