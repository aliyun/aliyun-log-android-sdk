package com.aliyun.sls.android;

import android.util.Log;

/**
 * @author gordon
 * @date 2021/04/14
 */
public final class SLSLog {
    private static final String TAG = "SLSAndroid";

    public static void v(String module, Object msg) {
        Log.v(TAG, format(module, msg));
    }

    public static void d(String module, Object msg) {
        Log.d(TAG, format(module, msg));
    }

    public static void w(String module, Object msg) {
        Log.w(TAG, format(module, msg));
    }

    public static void e(String module, Object msg) {
        Log.e(TAG, format(module, msg));
    }

    private static String format(String module, Object msg) {
        return String.format("module: %s, %s", module, toString(msg));
    }

    public static String format(String format, Object... args) {
        return String.format(format, args);
    }

    public static String toString(Object obj) {
        if (null == obj) {
            return "null";
        }

        if (obj instanceof String) {
            return (String)obj;
        }

        if (obj instanceof Number) {
            return obj.toString();
        }

        if (obj instanceof Boolean) {
            return String.valueOf(obj);
        }

        //if (obj instanceof List) {
        //    final List list = (List)obj;
        //    list.iterator();
        //    return "";
        //}
        //
        //if (obj instanceof Map) {
        //    final Map map = (Map)obj;
        //    map.entrySet().iterator().
        //    return "";
        //}

        return obj.toString();
    }

}
