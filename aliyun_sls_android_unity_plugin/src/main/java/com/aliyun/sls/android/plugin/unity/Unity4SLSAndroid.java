package com.aliyun.sls.android.plugin.unity;

import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicBoolean;

import android.app.Activity;
import android.util.Log;
import com.aliyun.sls.android.core.SLSAndroid;
import com.aliyun.sls.android.core.configuration.Configuration;
import com.aliyun.sls.android.core.configuration.Credentials;
import com.aliyun.sls.android.core.configuration.UserInfo;
import com.aliyun.sls.android.crashreporter.CrashReporter;
import com.aliyun.sls.android.crashreporter.CrashReporter.LogLevel;

/**
 * @author gordon
 * @date 2022/8/22
 */
public final class Unity4SLSAndroid {
    private static final AtomicBoolean hasInit = new AtomicBoolean(false);
    private static Configuration configuration;

    private static Activity getCurrentActivity() {
        try {
            Object object = Reflection.getStaticField("com.unity3d.player.UnityPlayer", "currentActivity", null);
            if (object instanceof Activity) { return (Activity)object; }
        } catch (Exception exception) {
            Log.w("SLSAndroidAgent", "Failed to get the current activity from UnityPlayer");
            exception.printStackTrace();
        }
        return null;
    }

    public static void initialize(Credentials credentials) {
        if (hasInit.get()) {
            return;
        }
        final Activity activity = getCurrentActivity();
        if (null == activity) {
            return;
        }

        SLSAndroid.initialize(activity.getApplicationContext(), credentials, configuration -> {
            Unity4SLSAndroid.configuration = configuration;
            configuration.enableCrashReporter = true;
        });
        hasInit.set(true);
    }

    public static void registerCredentialsCallback(CredentialsCallback callback) {
        SLSAndroid.registerCredentialsCallback((feature, result) -> {
            if (null != callback) {
                callback.onCall(feature, result.name());
            }
        });
    }

    public static void setLogLevel(int level) {
        SLSAndroid.setLogLevel(level);
    }

    public static void setCredentials(Credentials credentials) {
        SLSAndroid.setCredentials(credentials);
    }

    public static void setUserInfo(UserInfo info) {
        SLSAndroid.setUserInfo(info);
    }

    public static void setUserInfoExt(Map<String, String> ext) {
        if (null == ext) {
            return;
        }

        if (null == configuration || null == configuration.userInfo) {
            return;
        }

        for (Entry<String, String> entry : ext.entrySet()) {
            configuration.userInfo.addExt(entry.getKey(), entry.getValue());
        }
    }

    public static void setExtra(String key, Map<String, String> values) {
        SLSAndroid.setExtra(key, values);
    }

    public static void setExtra(String key, String value) {
        SLSAndroid.setExtra(key, value);
    }

    public static void removeExtra(String key) {
        SLSAndroid.removeExtra(key);
    }

    public static void clearExtra() {
        SLSAndroid.clearExtra();
    }

    public static void reportCustomLog(String type, String log) {
        CrashReporter.reportCustomLog(type, log);
    }

    public static void reportError(final String stacktrace) {
        reportError("exception", stacktrace);
    }

    public static void reportError(final String type, final String stacktrace) {
        reportError(type, "", stacktrace);
    }

    public static void reportError(final String type, final String message, final String stacktrace) {
        reportError(type, LogLevel.LOG_ERROR, message, stacktrace);
    }

    public static void reportError(
        final String type,
        final LogLevel level,
        final String message,
        final String stacktrace
    ) {
        CrashReporter.reportError(type, level, message, stacktrace);
    }

    public static void reportLuaError(
        final String message,
        final String stacktrace
    ) {
        reportLuaError(LogLevel.LOG_ERROR, message, stacktrace);
    }

    public static void reportLuaError(
        final LogLevel level,
        final String message,
        final String stacktrace
    ) {
        reportError("lua", level, message, stacktrace);
    }

    public static void reportCSharpError(
        final String message,
        final String stacktrace
    ) {
        reportCSharpError(LogLevel.LOG_ERROR, message, stacktrace);
    }

    public static void reportCSharpError(
        final LogLevel level,
        final String message,
        final String stacktrace
    ) {
        reportError("csharp", level, message, stacktrace);
    }

    public String helloFromAndroid(String appid) {
        return "hello from android, appid: " + appid;
    }

}
