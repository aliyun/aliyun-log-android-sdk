package com.aliyun.sls.android.core.utils;

/**
 * @author gordon
 * @date 2023/5/4
 */
public final class PrivacyUtils {
    private static boolean enablePrivacy = false;

    private PrivacyUtils() {
        //no instance
    }

    public static void setEnablePrivacy(boolean enable) {
        PrivacyUtils.enablePrivacy = enable;
    }

    public static boolean isEnablePrivacy() {
        return PrivacyUtils.enablePrivacy;
    }
}
