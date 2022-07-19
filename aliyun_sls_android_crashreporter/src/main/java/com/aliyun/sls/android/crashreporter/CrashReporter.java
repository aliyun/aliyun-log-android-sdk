package com.aliyun.sls.android.crashreporter;

import java.util.Map;

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

    public static void addCustomError(final String eventId, final Map<String, String> properties) {
        if (null == crashReporterFeature) {
            return;
        }

        crashReporterFeature.addCustom(eventId, properties);
    }
}
