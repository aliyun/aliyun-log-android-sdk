package com.aliyun.sls.android.producer.example.example.trace.http;

import android.content.Context;
import android.text.TextUtils;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;

import java.util.Arrays;
import java.util.List;

/**
 * @author gordon
 * @date 2021/10/18
 */
public final class SLSCookieManager {

    private static CookieManager cookieManager;

    static {
        cookieManager = CookieManager.getInstance();
    }


    private SLSCookieManager() {
        //no instance
    }

    public static void init(Context context) {
        CookieSyncManager.createInstance(context);
    }

    public static String getCookie() {
        return cookieManager.getCookie("_sls_demo_cookie");
    }

    public static void setCookie(String cookie) {
        cookieManager.setCookie("_sls_demo_cookie", cookie);
    }

    public static void clearCookie() {
        cookieManager.removeAllCookie();
    }

    public static void setCookie(List<String> cookies) {
        for (String cookie : cookies) {
            setCookie(cookie);
        }
    }

    public static void setCookie(String[] headers) {
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
                setCookie(Arrays.asList(header.split(";")));
                return;
            }
        }
    }
}
