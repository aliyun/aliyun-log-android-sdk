package com.aliyun.sls.android.utdid;

import java.security.MessageDigest;

import android.Manifest;
import android.content.Context;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import com.aliyun.sls.android.utils.PermissionHelper;

/**
 * @author gordon
 * @date 2021/05/24
 */
class Generator {

    public static String getImei(Context context) {
        if (!PermissionHelper.checkPermission(context, Manifest.permission.READ_PHONE_STATE)) {
            return "";
        }

        TelephonyManager tm = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
        if (VERSION.SDK_INT >= VERSION_CODES.Q) {
            try {
                return tm.getImei();
            } catch (Throwable e) {
                return "";
            }
        } else {
            return tm.getDeviceId();
        }
    }

    public static String getImsi(Context context) {
        if (!PermissionHelper.checkPermission(context, Manifest.permission.READ_PHONE_STATE)) {
            return "";
        }

        TelephonyManager tm = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
        try {
            return tm.getSubscriberId();
        } catch (Throwable e) {
            return "";
        }
    }

    public static String md5(String code) {
        try {
            byte[] bytes= MessageDigest.getInstance("MD5").digest(code.getBytes("UTF-8"));
            return bytes2Hex(bytes);
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return "";
    }

    private static final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();
    private static String bytes2Hex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = HEX_ARRAY[v >>> 4];
            hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
        }
        return new String(hexChars);
    }
}
