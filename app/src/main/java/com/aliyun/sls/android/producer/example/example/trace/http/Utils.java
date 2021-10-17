package com.aliyun.sls.android.producer.example.example.trace.http;

import android.text.TextUtils;

/**
 * @author gordon
 * @date 2021/10/17
 */
class Utils {
    private Utils() {
        //no instance
    }

    public static String getLoginId(String[] headers) {
        boolean found = false;
        for (String header : headers) {
            if (TextUtils.isEmpty(header)) {
                continue;
            }

            if (header.contains("Set-Cookie")) {
                found = true;
                continue;
            }

            if (found) {
                for (String pair : header.split(";")) {
                    if (pair.contains("logged_in")) {
                        return pair.split("=")[1];
                    }
                }

                return null;
            }
        }

        return null;
    }
}
