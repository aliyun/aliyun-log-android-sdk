package com.aliyun.sls.android.exporter.otlp;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author gordon
 * @date 2021/05/10
 */
class JsonUtil {

    private JsonUtil() {
        //no instance
    }

    public static boolean putOpt(JSONObject object, String key, Object value) {
        try {
            object.putOpt(key, value);
        } catch (JSONException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }
}
