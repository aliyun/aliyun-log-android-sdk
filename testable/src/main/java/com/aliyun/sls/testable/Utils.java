package com.aliyun.sls.testable;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author yulong.gyl
 * @date 2023/8/1
 */
public class Utils {
    private Utils() {
        //no instance
    }

    public static void put(JSONObject object, String key, Object value) {
        try {
            object.putOpt(key, value);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
