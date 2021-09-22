package com.aliyun.sls.android.producer.example.example.trace.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class UserUtils {
    private UserUtils() {
        //no instance
    }

    public static String getCachedUserId(Context context) {
        SharedPreferences sp = context.getSharedPreferences("user_info", Context.MODE_PRIVATE);
        return sp.getString("id", null);
    }

    public static void saveUserId(Context context, String userId) {
        context.getSharedPreferences("user_info", Context.MODE_PRIVATE)
                .edit()
                .putString("id", userId)
                .apply();
    }
}
