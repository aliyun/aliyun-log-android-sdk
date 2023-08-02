package com.aliyun.sls.android.producer.utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.SystemClock;
import com.aliyun.sls.android.producer.Log;
import com.aliyun.sls.android.producer.LogProducerHttpTool;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;

public class TimeUtils {

    private static long serverTime;
    private static long elapsedRealTime;

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
        if (null != context) {
            PackageManager pManager = context.getPackageManager();
            if (pManager.checkPermission(
                "android.permission.INTERNET"
                , context.getPackageName()) != PERMISSION_GRANTED) {
                return;
            }
        }

        final String host = endpoint.contains("http") ? endpoint.substring(endpoint.indexOf("://") + 3) : endpoint;
        final String url = "https://" + project + "." + host + "/servertime";
        final String[] headers = new String[] {"x-log-apiversion", "0.6.0"};
        ThreadUtils.exec(new Runnable() {
            @Override
            public void run() {
                LogProducerHttpTool.android_http_post(url, "GET", headers, new byte[] {});
            }
        });
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

    public static void fixTime(Log log) {
        if (null == log) {
            return;
        }

        final Map<String, String> contents = log.getContent();
        if (null == contents || contents.size() == 0) {
            return;
        }

        if (!contents.containsKey("local_timestamp")) {
            return;
        }

        Date date = new Date();
        String localTimestamp = contents.get("local_timestamp");
        // local_timestamp may invalid, check first
        if (localTimestamp.length() < 10) {
            localTimestamp = String.valueOf(System.currentTimeMillis());
        } else {
            localTimestamp = localTimestamp.substring(0, 10) + String.valueOf(date.getTime()).substring(10);
        }
        date.setTime(safe2Long(localTimestamp));
        String localTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS", Locale.getDefault()).format(date);

        contents.put("local_time_fixed", localTime);
        contents.put("local_timestamp_fixed", localTimestamp);
    }

    private static long safe2Long(String time) {
        try {
            return Long.parseLong(time);
        } catch (Throwable e) {
            return System.currentTimeMillis();
        }
    }

}
