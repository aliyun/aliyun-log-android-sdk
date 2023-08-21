package com.aliyun.sls.android.producer.utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

import android.content.Context;
import android.os.SystemClock;
import androidx.annotation.VisibleForTesting;
import com.aliyun.sls.android.producer.Log;
import com.aliyun.sls.android.producer.LogProducerHttpTool;

public class TimeUtils {

    @VisibleForTesting
    public static long serverTime;
    @VisibleForTesting
    public static long elapsedRealTime;

    private static class Holder {
        private static final TimeUtils INSTANCE = new TimeUtils();
    }

    private TimeUtils() {
        //no instance
    }

    public static TimeUtils getInstance() {
        return Holder.INSTANCE;
    }

    public void startUpdateServerTime(Context context, final String endpoint, final String project) {
        if (!Utils.checkInternetPermission(context)) {
            return;
        }

        final String url = getRequestUrl(endpoint, project);
        final String[] headers = getRequestHeader();
        ThreadUtils.exec(() -> LogProducerHttpTool.android_http_post(url, "GET", headers, new byte[] {}));
    }

    @VisibleForTesting
    public String getRequestUrl(String endpoint, String project) {
        final String host = endpoint.contains("http") ? endpoint.substring(endpoint.indexOf("://") + 3) : endpoint;
        return "https://" + project + "." + host + "/servertime";
    }

    @VisibleForTesting
    public String[] getRequestHeader() {
        return new String[] {"x-log-apiversion", "0.6.0"};
    }

    public void updateServerTime(long timeInMillis) {
        serverTime = timeInMillis;
        elapsedRealTime = SystemClock.elapsedRealtime();
    }

    public long getTimeInMillis() {
        if (0L == elapsedRealTime) {
            return System.currentTimeMillis() / 1000;
        }

        final long delta = (SystemClock.elapsedRealtime() - elapsedRealTime);
        return serverTime + (delta / 1000);
    }
}
