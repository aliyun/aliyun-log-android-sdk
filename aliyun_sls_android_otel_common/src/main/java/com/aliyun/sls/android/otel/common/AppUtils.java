package com.aliyun.sls.android.otel.common;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.text.TextUtils;

/**
 * @author gordon
 * @date 2021/04/19
 */
public class AppUtils {
    private static String appVersion;
    private static Integer appVersionCode = null;
    private static String appName;

    private static Long startNanoTime = null;
    private static Long endNanoTime = null;
    private static Boolean coldStart = null;
    private static boolean isForeground = false;
    private static String topActivity = null;

    private AppUtils() {
        //no instance
    }

    public static void setStartTime(final Long nanoTime) {
        if (null != AppUtils.startNanoTime) {
            return;
        }

        AppUtils.startNanoTime = nanoTime;
    }

    public static void setStartEnd() {
        if (null != AppUtils.endNanoTime) {
            return;
        }

        // TODO: 2022/4/20
        AppUtils.endNanoTime = System.nanoTime();
    }

    public static Long getStartNanoTime() {
        return AppUtils.startNanoTime;
    }

    public static Boolean isColdStart() {
        return coldStart;
    }

    public static void setColdStart(Boolean cold) {
        if (null != coldStart) {
            return;
        }

        coldStart = cold;
    }

    public static boolean isForeground() {
        return isForeground;
    }

    public static void setForeground(boolean foreground) {
        AppUtils.isForeground = foreground;
    }

    public static String getAppVersion(Context context) {
        if (!TextUtils.isEmpty(appVersion)) {
            return appVersion;
        }

        final PackageInfo info = getPackageInfo(context);
        if (null != info) {
            appVersionCode = info.versionCode;
            return appVersion = info.versionName;
        }
        return "";
    }

    public static int getAppVersionCode(Context context) {
        if (null != appVersionCode) {
            return appVersionCode;
        }

        final PackageInfo info = getPackageInfo(context);
        if (null != info) {
            appVersion = info.versionName;
            return appVersionCode = info.versionCode;
        }

        return 0;
    }

    public static String getAppName(Context context) {
        if (!TextUtils.isEmpty(appName)) {
            return appName;
        }

        final ApplicationInfo applicationInfo = context.getApplicationInfo();
        if (null != applicationInfo) {
            int resId = applicationInfo.labelRes;
            if (0 != resId) {
                return appName = context.getString(resId);
            }

            if (null != applicationInfo.nonLocalizedLabel) {
                return appName = applicationInfo.nonLocalizedLabel.toString();
            }

            return context.getPackageName();
        }

        return "";
    }

    private static PackageInfo getPackageInfo(Context context) {
        try {
            return context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
        } catch (NameNotFoundException e) {
            return null;
        }
    }

    public static String getTopActivity() {
        return topActivity;
    }

    public static void setTopActivity(String topActivity) {
        AppUtils.topActivity = topActivity;
    }

    public static boolean debuggable(Context context) {
        try {
            return (context.getApplicationInfo().flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0;
        } catch (Throwable t) {
            return false;
        }
    }
}
