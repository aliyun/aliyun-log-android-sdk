package com.aliyun.sls.android.producer.utils;

import java.lang.reflect.Method;

import android.annotation.SuppressLint;
import android.app.Application;
import android.os.Build.VERSION;
import android.os.Handler;
import android.os.Looper;

/**
 * @author gordon
 * @date 2023/5/15
 */
@SuppressLint("PrivateApi")
public final class ContextUtils {
    private static Class<?> sActivityThread = null;
    private static Application sApplication;

    static {
        try {
            sActivityThread = Class.forName("android.app.ActivityThread");
        } catch (Throwable ignored) {
        }
    }

    private ContextUtils() {
        //no instance
    }

    public static synchronized Application getApplication() {
        if (null != sApplication) {
            return sApplication;
        }

        if (null == sActivityThread) {
            return null;
        }

        Object activityThreadObjc = getActivityThread();
        if (null == activityThreadObjc) {
            return null;
        }

        try {
            Method getApplicationMethod = activityThreadObjc.getClass().getMethod("getApplication");
            getApplicationMethod.setAccessible(true);
            return sApplication = (Application)getApplicationMethod.invoke(activityThreadObjc, (Object[])null);
        } catch (Throwable ignored) {
            return null;
        }
    }

    @SuppressLint("PrivateApi")
    public static synchronized Object getActivityThread() {
        try {
            if (null == sActivityThread) {
                return null;
            }

            final Method currentActivityThreadMethod = sActivityThread.getMethod("currentActivityThread");
            currentActivityThreadMethod.setAccessible(true);

            if (Thread.currentThread().getId() == Looper.getMainLooper().getThread().getId() || VERSION.SDK_INT >= 18) {
                return currentActivityThreadMethod.invoke(null);
            }

            // api < 18 时，需要在主线程获取
            final Object lock = new Object();
            final Object[] output = new Object[1];
            synchronized (lock) {
                new Handler(Looper.getMainLooper()).post(() -> {
                    try {
                        output[0] = currentActivityThreadMethod.invoke(null);
                    } catch (Throwable ignored) {
                    } finally {
                        try {
                            lock.notify();
                        } catch (Throwable ignored) {
                        }
                    }
                });
            }

            try {
                lock.wait();
            } catch (Throwable ignored) {
                return null;
            }

            return output[0];
        } catch (Throwable e) {
            e.printStackTrace();
            return null;
        }
    }
}
