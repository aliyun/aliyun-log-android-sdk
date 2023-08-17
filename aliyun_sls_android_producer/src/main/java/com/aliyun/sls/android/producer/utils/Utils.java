package com.aliyun.sls.android.producer.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;

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
        // if context not set from content provider, we get context from activity again.
        // under multi-process, content provider may not get the context.
        // cache the context will be performance friendly.
        if (null == context) {
            context = ContextUtils.getApplication();
        }

        return context;
    }

    public static void setContext(Context context) {
        Utils.context = null != context ? context.getApplicationContext() : null;
    }

    public static boolean checkInternetPermission(Context context) {
        if (null == context) {
            return false;
        }

        PackageManager pManager = context.getPackageManager();
        return pManager.checkPermission(
            "android.permission.INTERNET"
            , context.getPackageName()) == PERMISSION_GRANTED;
    }

}
