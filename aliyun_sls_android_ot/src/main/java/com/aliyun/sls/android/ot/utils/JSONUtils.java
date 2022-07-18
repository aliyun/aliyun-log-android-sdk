package com.aliyun.sls.android.ot.utils;

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
}
