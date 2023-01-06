package com.aliyun.sls.android.crashreporter;

import java.io.PrintWriter;
import java.io.StringWriter;

import android.annotation.SuppressLint;

/**
 * @author gordon
 * @date 2022/7/19
 */
public class CrashReporter {
    public enum LogLevel {
        LOG_VERBOSE,
        LOG_DEBUG,
        LOG_INFO,
        LOG_WARNING,
        LOG_ASSERT,
        LOG_ERROR,
        LOG_EXCEPTION
    }

    @SuppressLint("StaticFieldLeak")
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

    public static void reportCustomLog(String type, String log) {
        if (null == crashReporterFeature) {
            return;
        }

        crashReporterFeature.reportCustomLog(type, log);
    }

    public static void reportError(final String stacktrace) {
        reportError("exception", stacktrace);
    }

    public static void reportError(final Throwable t) {
        reportError("exception", t);
    }

    public static void reportError(final String type, final Throwable t) {
        reportError(type, "", t);
    }

    public static void reportError(final String type, final String stacktrace) {
        reportError(type, "", stacktrace);
    }

    public static void reportError(final String type, final String message, final String stacktrace) {
        reportError(type, LogLevel.LOG_ERROR, message, stacktrace);
    }

    public static void reportError(final String type, final String message, final Throwable t) {
        reportError(type, LogLevel.LOG_ERROR, message, t);
    }

    public static void reportError(final String type, final LogLevel level, final String message, final Throwable t) {
        reportError(type, level, message, stacktrace2string(t));
    }

    public static void reportError(
        final String type,
        final LogLevel level,
        final String message,
        final String stacktrace
    ) {
        if (null == crashReporterFeature) {
            return;
        }

        crashReporterFeature.reportError(type, level, message, stacktrace);
    }

    public static void reportLuaError(
        final LogLevel level,
        final String message,
        final String stacktrace
    ) {
        reportError("lua", level, message, stacktrace);
    }

    public static void reportCSharpError(
        final LogLevel level,
        final String message,
        final String stacktrace
    ) {
        reportError("csharp", level, message, stacktrace);
    }

    private static String stacktrace2string(Throwable t) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        t.printStackTrace(pw);
        return sw.toString();
    }
}
