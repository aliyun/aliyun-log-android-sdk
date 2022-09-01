package com.aliyun.sls.android.utdid;

import java.security.MessageDigest;

import android.content.Context;

/**
 * @author gordon
 * @date 2021/05/24
 */
class Generator {

    public static String getImei(Context context) {
        return "";
    }

    public static String getImsi(Context context) {
        return "";
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
