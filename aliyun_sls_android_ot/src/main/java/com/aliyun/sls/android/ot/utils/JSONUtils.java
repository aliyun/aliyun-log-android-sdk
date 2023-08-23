package com.aliyun.sls.android.ot.utils;

import android.text.TextUtils;
import android.util.Pair;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author gordon
 * @date 2022/4/13
 */
public class JSONUtils {
    private JSONUtils() {
        //no instance
    }

    public static JSONObject put(JSONObject object, String key, Object value) {
        try {
            object.putOpt(key, value);
        } catch (JSONException e) {
            // ignore
        }
        return object;
    }

    @SafeVarargs
    public static JSONObject object(Pair<String, Object>... kvs) {
        JSONObject object = new JSONObject();
        if (null != kvs) {
            for (Pair<String, Object> kv : kvs) {
                if (TextUtils.isEmpty(kv.first)) {
                    continue;
                }

                put(object, kv.first, kv.second);
            }
        }
        return object;
    }
}
