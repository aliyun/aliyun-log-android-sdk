package com.aliyun.sls.android.producer.utils;

import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.UnknownHostException;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Build.VERSION;

/**
 * @author gordon
 * @date 2022/9/16
 */
public final class Utils {

    @SuppressLint("StaticFieldLeak")
    private static Context context;
    private static boolean finalNetworkConnected = true;

    private Utils() {
        //no instance
    }

    public static Context getContext() {
        return context;
    }

    public static void setContext(Context context) {
        Utils.context = null != context ? context.getApplicationContext() : null;
    }

    public static boolean checkInternetConnection() {
        checkInternetConnectionInternal();
        return finalNetworkConnected;
    }

    private static void checkInternetConnectionInternal() {
        if (null == context) {
            finalNetworkConnected = true;
            return;
        }

        if (!checkPermission("android.permission.ACCESS_NETWORK_STATE")) {
            // always connected if not have the network state permission
            finalNetworkConnected = true;
            return;
        }

        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        final boolean connected = cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnectedOrConnecting();
        // check again if no connection
        if (!connected || !finalNetworkConnected) {
            ConnectionChecker.start();
        }
    }

    public static boolean checkPermission(String permission) {
        if (null == context) {
            return false;
        }

        if (VERSION.SDK_INT >= 23) {
            try {
                Class<?> clazz = Class.forName("android.content.Context");
                Method method = clazz.getMethod("checkSelfPermission", String.class);
                Integer rest = (Integer)method.invoke(context, permission);
                if (null != rest) {
                    return rest == PackageManager.PERMISSION_GRANTED;
                }

                return false;
            } catch (Throwable e) {
                return false;
            }
        } else {
            // return context.checkPermission(permission, Binder.getCallingPid(), Binder.getCallingUid()) ==
            // PackageManager.PERMISSION_GRANTED;
            PackageManager pm = context.getPackageManager();
            return pm.checkPermission(permission, context.getPackageName()) == PackageManager.PERMISSION_GRANTED;
        }
    }

    private static class ConnectionChecker {
        private static boolean checking = false;

        private static synchronized void start() {
            if (checking) {
                return;
            }

            checking = true;

            new AsyncTask<Void, Void, Void>() {

                @Override
                protected Void doInBackground(Void... voids) {
                    try {
                        InetAddress address = InetAddress.getByName("www.aliyun.com");
                        Utils.finalNetworkConnected = null != address && !address.getHostAddress().equals("");
                    } catch (UnknownHostException e) {
                        Utils.finalNetworkConnected  = false;
                    } finally {
                        checking = false;
                    }
                    return null;
                }
            }.executeOnExecutor(ThreadUtils.cachedExecutors());

        }
    }
}
