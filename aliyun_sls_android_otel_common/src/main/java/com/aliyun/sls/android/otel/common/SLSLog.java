package com.aliyun.sls.android.otel.common;

import android.util.Log;

/**
 * @author gordon
 * @date 2021/04/14
 */
public final class SLSLog {
    private static final String TAG = "SLSAndroid";
    private static int level = Log.INFO;

    public static void setLevel(int level) {
        SLSLog.level = level;
    }

    public static void v(String module, Object msg) {
        if (level <= Log.VERBOSE) {
            Log.v(TAG, format(module, msg));
        }
    }

    public static void d(String module, Object msg) {
        if (level <= Log.DEBUG) {
            Log.d(TAG, format(module, msg));
        }
    }

    public static void i(String module, Object msg) {
        if (level <= Log.INFO) {
            Log.i(TAG, format(module, msg));
        }
    }

    public static void w(String module, Object msg) {
        if (level <= Log.WARN) {
            Log.w(TAG, format(module, msg));
        }
    }

    public static void e(String module, Object msg) {
        if (level <= Log.ERROR) {
            Log.e(TAG, format(module, msg));
        }
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
