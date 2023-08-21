package com.aliyun.sls.android.producer.example.utils;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.preference.PreferenceManager;
import com.aliyun.sls.android.producer.BuildConfig;

/**
 * @author gordon
 * @date 2021/07/26
 */
public class PreferenceUtils {
    private PreferenceUtils() {
        //no instance
    }

    public static void registerOnSharedPreferenceChangeListener(Context context, SharedPreferences.OnSharedPreferenceChangeListener listener) {
        PreferenceManager.getDefaultSharedPreferences(context).registerOnSharedPreferenceChangeListener(listener);
    }

    public static void unregisterOnSharedPreferenceChangeListener(Context context, SharedPreferences.OnSharedPreferenceChangeListener listener) {
        PreferenceManager.getDefaultSharedPreferences(context).unregisterOnSharedPreferenceChangeListener(listener);
    }

    public static boolean isForceHttp(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getBoolean("https_force_http", false);
    }

    public static String getPluginAppId(Context context) {
        if (BuildConfig.CONFIG_ENABLE) {
            return BuildConfig.PLUGIN_APPID;
        }

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getString("settings_plugin_appid", "");
    }

    public static String getAccessKeyId(Context context) {
        if (BuildConfig.CONFIG_ENABLE) {
            return BuildConfig.ACCESS_KEYID;
        }

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getString("settings_key_id", "");
    }

    public static String getAccessKeySecret(Context context) {
        if (BuildConfig.CONFIG_ENABLE) {
            return BuildConfig.ACCESS_KEY_SECRET;
        }

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getString("settings_key_secret", "");
    }

    public static String getAccessKeyToken(Context context) {
        if (BuildConfig.CONFIG_ENABLE) {
            return BuildConfig.ACCESS_KEY_TOKEN;
        }

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getString("settings_key_token", "");
    }

    public static String getLogProject(Context context) {
        if (BuildConfig.CONFIG_ENABLE) {
            return BuildConfig.LOG_PROJECT;
        }

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getString("settings_project", "");
    }

    public static String getLogStore(Context context) {
        if (BuildConfig.CONFIG_ENABLE) {
            return BuildConfig.LOG_STORE;
        }

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getString("settings_store", "");
    }

    public static String getEndpoint(Context context) {
        if (BuildConfig.CONFIG_ENABLE) {
            return BuildConfig.END_POINT;
        }

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getString("settings_end_point", "");
    }

    public static String getNetworkSecKey(Context context) {
        if (BuildConfig.CONFIG_ENABLE) {
            return BuildConfig.NETWORK_SECKEY;
        }

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getString("network_seckey", "");
    }

    public static void overrideConfig(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.edit()
                .putString("settings_plugin_appid", BuildConfig.PLUGIN_APPID)
                .putString("settings_key_id", BuildConfig.ACCESS_KEYID)
                .putString("settings_key_secret", BuildConfig.ACCESS_KEY_SECRET)
                .putString("settings_key_token", BuildConfig.ACCESS_KEY_TOKEN)
                .putString("settings_end_point", BuildConfig.END_POINT)
                .putString("settings_project", BuildConfig.LOG_PROJECT)
                .putString("settings_store", BuildConfig.LOG_STORE)
                .putString("network_seckey", BuildConfig.NETWORK_SECKEY)
        .apply();


    }
}
