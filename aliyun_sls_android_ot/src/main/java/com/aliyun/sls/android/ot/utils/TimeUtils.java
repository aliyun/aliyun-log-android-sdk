package com.aliyun.sls.android.ot.utils;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import android.os.SystemClock;

/**
 * @author gordon
 * @date 2022/4/11
 */
public enum TimeUtils {
    instance;
    private final long start = TimeUnit.MILLISECONDS.toNanos(System.currentTimeMillis());
    private final long nanoTime = System.nanoTime();

    public Long getTime() {
        return getDate().getTime();
    }

    public Date getDate() {
        return Calendar.getInstance(TimeZone.getTimeZone("UTC")).getTime();
    }

    public Long getUptimeMillis() {
        return SystemClock.uptimeMillis();
    }

    public Long now() {
        return start + (System.nanoTime() - nanoTime);
    }
}
