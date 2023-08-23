package com.aliyun.sls.android.producer.utils;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.Application;
import android.content.Context;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.text.TextUtils;

/**
 * @author gordon
 * @date 2023/5/11
 */
public final class ProcessUtils {
    private static String sProcessName;

    public static String getCurrentProcessName(Context context) {
        if (!TextUtils.isEmpty(sProcessName)) {
            return sProcessName;
        }

        String processName = getProcessNameByApplication();
        if (!TextUtils.isEmpty(processName)) {
            sProcessName = processName;
            return sProcessName;
        }

        processName = getProcessNameViaLinuxFile();
        if (!TextUtils.isEmpty(processName)) {
            sProcessName = processName;
            return sProcessName;
        }

        processName = getProcessNameViaActivityManager(context);
        if (!TextUtils.isEmpty(processName)) {
            sProcessName = processName;
            return sProcessName;
        }

        return sProcessName;
    }

    public static boolean isMainProcess(Context context) {
        return isInProcess(context, context.getPackageName());
    }

    public static boolean isInProcess(Context context, String processName) {
        String currentProcessName = getCurrentProcessName(context);
        return processName != null && processName.equalsIgnoreCase(currentProcessName);
    }

    private static String getProcessNameByApplication() {
        if (VERSION.SDK_INT >= VERSION_CODES.P) {
            return Application.getProcessName();
        }
        return null;
    }

    private static String getProcessNameViaLinuxFile() {
        int pid = android.os.Process.myPid();
        String line = "/proc/" + pid + "/cmdline";
        FileInputStream fis = null;
        String processName = null;
        byte[] bytes = new byte[1024];
        int read = 0;

        try {
            fis = new FileInputStream(line);
            read = fis.read(bytes);
        } catch (Exception ignored) {
        } finally {
            try {
                if (fis != null) {
                    fis.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (read > 0) {
            processName = new String(bytes, 0, read);
            processName = processName.trim();
        }

        return processName;
    }

    private static String getProcessNameViaActivityManager(Context context) {
        if (context == null) {
            return null;
        }

        int pid = android.os.Process.myPid();
        ActivityManager mActivityManager = (ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE);
        if (mActivityManager == null) {
            return null;
        }
        List<RunningAppProcessInfo> processes = mActivityManager.getRunningAppProcesses();
        if (processes == null) {
            return null;
        }
        for (RunningAppProcessInfo appProcess : processes) {
            if (appProcess != null && appProcess.pid == pid) {
                return appProcess.processName;
            }
        }

        return null;
    }
}
