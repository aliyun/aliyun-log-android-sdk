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

    public String helloFromAndroid(String appid) {
        return "hello from android, appid: " + appid;
    }

}
