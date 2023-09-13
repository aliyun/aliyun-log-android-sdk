package com.aliyun.sls.android.otel.common;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;

/**
 * Util for checkout device rootable.
 *
 * @author gordon
 * @date 2021/04/19
 */
class RootUtil {
    private static boolean hasChecked = false;
    private static boolean rooted = false;

    public static synchronized boolean isDeviceRooted() {
        if (hasChecked) {
            return rooted;
        }
        hasChecked = true;
        rooted = checkRootMethod1() || checkRootMethod2() || checkRootMethod3();
        return rooted;
    }

    private static boolean checkRootMethod1() {
        String buildTags = android.os.Build.TAGS;
        return buildTags != null && buildTags.contains("test-keys");
    }

    private static boolean checkRootMethod2() {
        String[] paths = {"/system/app/Superuser.apk", "/sbin/su", "/system/bin/su", "/system/xbin/su",
            "/data/local/xbin/su", "/data/local/bin/su", "/system/sd/xbin/su",
            "/system/bin/failsafe/su", "/data/local/su", "/su/bin/su"};
        for (String path : paths) {
            if (new File(path).exists()) {return true;}
        }
        return false;
    }

    private static boolean checkRootMethod3() {
        Process process = null;
        try {
            process = Runtime.getRuntime().exec(new String[] {"/system/xbin/which", "su"});
            BufferedReader in = new BufferedReader(new InputStreamReader(process.getInputStream()));
            if (in.readLine() != null) {return true;}
            return false;
        } catch (Throwable t) {
            return false;
        } finally {
            if (process != null) {process.destroy();}
        }
    }
}