package com.aliyun.sls.android.producer.example.example.trace.utils;

import android.content.Context;
import android.text.TextUtils;
import com.aliyun.sls.android.producer.example.SLSGlobal;
import com.aliyun.sls.android.producer.example.example.trace.model.UserModel;

/**
 * @author gordon
 * @date 2021/10/17
 */
public class UserUtils {
    private UserUtils() {
        //no instance
    }

    private static String loginId = null;
    public static UserModel userModel = null;

    public static void setLoginId(String loginId) {
        SLSGlobal.application.getSharedPreferences("_login", Context.MODE_PRIVATE)
                .edit()
                .putString("id", loginId)
                .apply();
    }

    public static String getLoginId() {
        if (TextUtils.isEmpty(loginId)) {
            loginId = SLSGlobal.application.getSharedPreferences("_login", Context.MODE_PRIVATE)
                    .getString("id", null);
        }

        return loginId;
    }
}
