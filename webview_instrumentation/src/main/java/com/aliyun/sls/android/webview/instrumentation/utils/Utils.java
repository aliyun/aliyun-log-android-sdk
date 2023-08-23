package com.aliyun.sls.android.webview.instrumentation.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

/**
 * @author yulong.gyl
 * @date 2023/6/25
 */
public class Utils {
    private static final int BUFFER_SIZE = 8192;

    private static final String URL_REQUEST_ID_REGEX = "^.*?[&|\\?|%3f]?otel_flag[=|%3d](\\d+).*?$";
    private static final String URL_REQUEST_ID_PAIR_REGEX = "^.*?([&|\\?|%3f]?otel_flag[=|%3d]\\d+).*?$";

    private Utils() {
        //no instance
    }

    public static String readFromAssets(Context context, String filePath) {
        try {
            InputStream ins = context.getApplicationContext().getAssets().open(filePath);
            byte[] bytes = input2Bytes(ins);
            if (null == bytes) {
                return null;
            }

            return new String(bytes, "UTF-8");
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
    }

    public static String input2String(final InputStream ins, String charsetName) {
        try {
            return new String(input2Bytes(ins), TextUtils.isEmpty(charsetName) ? "utf-8" : charsetName);
        } catch (Throwable e) {
            return null;
        }
    }

    public static byte[] input2Bytes(final InputStream ins) {
        if (null == ins) {
            return null;
        }
        return input2OutputStream(ins).toByteArray();
    }

    public static ByteArrayOutputStream input2OutputStream(final InputStream ins) {
        if (null == ins) {
            return null;
        }
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            byte[] bytes = new byte[BUFFER_SIZE];
            int len = 0;
            while (((len = ins.read(bytes, 0, BUFFER_SIZE)) != -1)) {
                bos.write(bytes, 0, len);
            }
            return bos;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } finally {
            try {
                ins.close();
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }
    }

    public static InputStream string2Input(String str) {
        if (null == str) {
            return null;
        }
        try {
            return new ByteArrayInputStream(str.getBytes("utf-8"));
        } catch (Throwable e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String fetchRequestIdPair(String url) {
        Pattern pattern =  Pattern.compile(URL_REQUEST_ID_PAIR_REGEX, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(url);
        if (matcher.find()) {
            int count = matcher.groupCount() + 1;
            for (int i = 0; i < count; i++) {
                if (1 == i) {
                    String content = matcher.group(i);
                    Log.d("DEBUGGG", content);
                    return content;
                }
            }
        }
        return null;
    }
}
