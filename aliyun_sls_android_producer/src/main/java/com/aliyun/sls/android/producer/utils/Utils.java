package com.aliyun.sls.android.producer.utils;

import android.annotation.SuppressLint;
import android.content.Context;

/**
 * @author gordon
 * @date 2022/9/16
 */
public final class Utils {

    @SuppressLint("StaticFieldLeak")
    private static Context context;

    private Utils() {
        //no instance
    }

    public static Context getContext() {
        return context;
    }

    public static void setContext(Context context) {
        Utils.context = null != context ? context.getApplicationContext() : null;
    }

}
