package com.aliyun.sls.android.scheme;

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
    private static String packageName;
    private static String appVersion;
    private static String appName;

    private AppUtils() {
        //no instance
    }

    public static String getPackageName(Context context) {
        if (!TextUtils.isEmpty(packageName)) {
            return packageName;
        }
        return packageName = context.getPackageName();
    }

    public static String getAppVersion(Context context) {
        if (!TextUtils.isEmpty(appVersion)) {
            return appVersion;
        }

        final PackageInfo info = getPackageInfo(context);
        if (null != info) {
            return appVersion = info.versionName;
        }
        return "";
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

    private static ApplicationInfo getApplicationInfo(Context context) {
        return context.getApplicationInfo();
    }
}
