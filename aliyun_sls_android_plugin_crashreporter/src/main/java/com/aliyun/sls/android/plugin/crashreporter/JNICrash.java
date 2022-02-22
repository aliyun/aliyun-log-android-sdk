package com.aliyun.sls.android.plugin.crashreporter;

import com.uc.crashsdk.JNIBridge;

/**
 * @author gordon
 * @date 2022/2/17
 */
public class JNICrash {

    public static void nativeCrash(int type) {
        JNIBridge.nativeCrash(type, 0);
    }
}
