package com.aliyun.sls.android.producer.internal;

import java.util.Arrays;

import com.aliyun.sls.android.producer.BuildConfig;

/**
 * @author gordon
 * @date 2022/9/19
 */
public final class HttpHeader {

    private static final String DEFAULT_UA = "sls-android-sdk/" + BuildConfig.VERSION_NAME;

    private HttpHeader() {
        //no instance
    }

    public static String[] getHeadersWithUA(String[] srcHeaders, String... ua) {
        String[] headers = Arrays.copyOf(srcHeaders, srcHeaders.length + 2);
        headers[srcHeaders.length] = "User-agent";
        if (null == ua || ua.length == 0) {
            headers[srcHeaders.length + 1] = DEFAULT_UA;
        } else {
            StringBuilder uaBuilder = new StringBuilder(DEFAULT_UA);
            for (String s : ua) {
                uaBuilder.append(";");
                uaBuilder.append(s);
            }
            headers[srcHeaders.length + 1] = uaBuilder.substring(0, uaBuilder.length());
        }

        return headers;
    }

}
