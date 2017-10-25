package com.aliyun.sls.android.sdk.utils;


import com.aliyun.sls.android.sdk.LogException;
import com.aliyun.sls.android.sdk.core.auth.HmacSHA1Signature;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;
import java.util.zip.Deflater;


/**
 * Created by zhouzhuo on 11/22/15.
 */
public class Utils {

    public static byte[] GzipFrom(byte[] jsonByte) throws LogException {
        ByteArrayOutputStream out = null;
        Deflater compresser = new Deflater();
        try {
            out = new ByteArrayOutputStream(jsonByte.length);
            compresser.setInput(jsonByte);
            compresser.finish();
            byte[] buf = new byte[10240];
            while (compresser.finished() == false) {
                int count = compresser.deflate(buf);
                out.write(buf, 0, count);
            }
            return out.toByteArray();
        } catch (Exception e) {
            throw new LogException("LogClientError", "fail to zip data", "");
        } finally {
            compresser.end();
            try {
                if (out.size() != 0) out.close();
            } catch (IOException e) {
            }
        }
    }

    public static String ParseToMd5U32(byte[] bytes) throws LogException {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            String res = new BigInteger(1, md.digest(bytes)).toString(16).toUpperCase();
            StringBuilder zeros = new StringBuilder();
            for (int i = 0; i + res.length() < 32; i++) {
                zeros.append("0");
            }
            return zeros.toString() + res;
        } catch (NoSuchAlgorithmException e) {
            throw new LogException("LogClientError", "Not Supported signature method " + "MD5", e, "");
        }
    }


    public static String GetMGTTime() {
        Calendar cd = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
        sdf.setTimeZone(TimeZone.getTimeZone("GMT")); // 设置时区为GMT
        String str = sdf.format(cd.getTime());
        return str;
    }

//    public static String hmac_sha1(String encryptText, String encryptKey) throws Exception {
//        byte[] keyBytes = encryptKey.getBytes("UTF-8");
//        byte[] dataBytes = encryptText.getBytes("UTF-8");
//        Mac mac = Mac.getInstance("HmacSHA1");
//        mac.init(new SecretKeySpec(keyBytes, "HmacSHA1"));
//        return new String(Base64Kit.encode(mac.doFinal(dataBytes)));
//    }

    public static void assertTrue(boolean condition, String message) {
        if (!condition) {
            throw new IllegalArgumentException(message);
        }
    }

    /**
     * 根据ak/sk、content生成token
     *
     * @param accessKey
     * @param screctKey
     * @param content
     * @return
     */
    public static String sign(String accessKey, String screctKey, String content) {

        String signature = null;

        try {
            signature = new HmacSHA1Signature().computeSignature(screctKey, content);
            signature = signature.trim();
        } catch (Exception e) {
            throw new IllegalStateException("Compute signature failed!", e);
        }

        return "LOG " + accessKey + ":" + signature;
    }


}
