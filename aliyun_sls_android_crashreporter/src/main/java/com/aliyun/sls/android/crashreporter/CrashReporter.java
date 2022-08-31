package com.aliyun.sls.android.crashreporter;

/**
 * @author gordon
 * @date 2022/7/19
 */
public class CrashReporter {
    private static CrashReporterFeature crashReporterFeature;

    private CrashReporter() {
        //no instance
    }

    static void setCrashReporterFeature(CrashReporterFeature crashReporterFeature) {
        CrashReporter.crashReporterFeature = crashReporterFeature;
    }

    public static void setEnabled(boolean enable) {
        if (null == crashReporterFeature) {
            return;
        }

        crashReporterFeature.setFeatureEnabled(enable);
    }
}
