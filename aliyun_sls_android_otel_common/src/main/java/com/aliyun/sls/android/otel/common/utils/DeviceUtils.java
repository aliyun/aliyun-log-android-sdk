//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.aliyun.sls.android.otel.common.utils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Locale;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import com.aliyun.sls.android.otel.common.utdid.Utdid;

/**
 * Utils set for get device info.
 *
 * @author gordon
 * @date 2021/04/19
 */
public class DeviceUtils {
    private static final String TAG = "DeviceUtils";

    public static final String NETWORK_CLASS_WIFI = "Wi-Fi";
    private static final String NETWORK_CLASS_2_G = "2G";
    private static final String NETWORK_CLASS_3_G = "3G";
    private static final String NETWORK_CLASS_4_G = "4G";
    private static final String NETWORK_CLASS_UNKNOWN = "Unknown";
    private static String cpuName = null;
    private static final String[] NETWORK_INFO_DEFAULT = new String[] {"Unknown", "Unknown"};
    private static final String[] NETWORK_INFO = new String[] {"Unknown", "Unknown"};
    private static String imsi = null;
    private static String imei = null;

    private DeviceUtils() {
    }

    public static String getCpuName() {
        if (cpuName != null) {
            return cpuName;
        } else {
            String str1 = "/proc/cpuinfo";
            String str2 = "";
            FileReader fr = null;
            BufferedReader localBufferedReader = null;

            try {
                fr = new FileReader(str1);
                localBufferedReader = new BufferedReader(fr);

                while ((str2 = localBufferedReader.readLine()) != null) {
                    if (str2.contains("Hardware")) {
                        cpuName = str2.split(":")[1];
                        String var4 = cpuName;
                        return var4;
                    }
                }
            } catch (IOException var15) {
            } finally {
                try {
                    if (fr != null) {
                        fr.close();
                    }

                    if (localBufferedReader != null) {
                        localBufferedReader.close();
                    }
                } catch (Exception var14) {
                }

            }

            return null;
        }
    }

    public static String getCarrier(Context context) {
        try {
            TelephonyManager telephonyManager = (TelephonyManager)context.getSystemService(
                Context.TELEPHONY_SERVICE);
            return telephonyManager.getNetworkOperatorName();
        } catch (Exception var2) {
            return null;
        }
    }

    public static String getAccessName(Context context) {
        return DeviceUtils.getNetworkType(context)[0];
    }

    public static String getAccessSubTypeName(Context context) {
        String[] networkStatus = DeviceUtils.getNetworkType(context);
        String accessName = networkStatus[0];
        if (networkStatus.length > 1 && accessName != null && !"Wi-Fi".equals(accessName)) {
            return networkStatus[1];
        }

        return NETWORK_INFO_DEFAULT[1];
    }

    @SuppressLint({"WrongConstant", "MissingPermission"})
    public static String[] getNetworkType(Context context) {
        if (context == null) {
            return NETWORK_INFO_DEFAULT;
        } else {
            try {
                ConnectivityManager cManager = (ConnectivityManager)context.getSystemService(
                    Context.CONNECTIVITY_SERVICE);
                if (cManager == null) {
                    return NETWORK_INFO_DEFAULT;
                }

                NetworkInfo nInfo = cManager.getActiveNetworkInfo();
                if (nInfo == null) {
                    return NETWORK_INFO_DEFAULT;
                }

                if (nInfo.isConnected()) {
                    if (nInfo.getType() == 1) {
                        NETWORK_INFO[0] = "Wi-Fi";
                        return NETWORK_INFO;
                    }

                    if (nInfo.getType() == 0) {
                        NETWORK_INFO[0] = getNetworkClass(nInfo.getSubtype());
                        NETWORK_INFO[1] = nInfo.getSubtypeName();
                        return NETWORK_INFO;
                    }
                }
            } catch (Throwable var4) {
                // ignore
            }

            return NETWORK_INFO_DEFAULT;
        }
    }

    private static String getNetworkClass(int networkType) {
        switch (networkType) {
            case TelephonyManager.NETWORK_TYPE_GPRS:
            case TelephonyManager.NETWORK_TYPE_EDGE:
            case TelephonyManager.NETWORK_TYPE_CDMA:
            case TelephonyManager.NETWORK_TYPE_1xRTT:
            case TelephonyManager.NETWORK_TYPE_IDEN:
            case TelephonyManager.NETWORK_TYPE_GSM:
                return "2G";
            case TelephonyManager.NETWORK_TYPE_UMTS:
            case TelephonyManager.NETWORK_TYPE_EVDO_0:
            case TelephonyManager.NETWORK_TYPE_EVDO_A:
            case TelephonyManager.NETWORK_TYPE_HSDPA:
            case TelephonyManager.NETWORK_TYPE_HSUPA:
            case TelephonyManager.NETWORK_TYPE_HSPA:
            case TelephonyManager.NETWORK_TYPE_EVDO_B:
            case TelephonyManager.NETWORK_TYPE_EHRPD:
            case TelephonyManager.NETWORK_TYPE_HSPAP:
            case TelephonyManager.NETWORK_TYPE_TD_SCDMA:
                return "3G";
            case TelephonyManager.NETWORK_TYPE_LTE:
            case TelephonyManager.NETWORK_TYPE_IWLAN:
                return "4G";
            case TelephonyManager.NETWORK_TYPE_NR:
                return "5G";
            default:
                return "Unknown";
        }
    }

    public static String getLanguage() {
        try {
            return Locale.getDefault().getLanguage();
        } catch (Exception var1) {
            SLSLog.e(TAG, "get country error: " + var1.getMessage());
            return null;
        }
    }

    public static String getCountry() {
        try {
            return Locale.getDefault().getCountry();
        } catch (Exception var1) {
            SLSLog.e(TAG, "get country error: " + var1.getMessage());
            return null;
        }
    }

    public static String getResolution(Context context) {
        String resolution = "Unknown";

        try {
            DisplayMetrics dm = context.getResources().getDisplayMetrics();
            int width = dm.widthPixels;
            int height = dm.heightPixels;
            if (width > height) {
                width ^= height;
                height ^= width;
                width ^= height;
            }

            resolution = width + "x" + height;
        } catch (Exception var5) {
            SLSLog.e(TAG, "DeviceUtils getResolution: error: " + var5.getMessage());
        }

        return resolution;
    }

    public static String getUtdid(Context context) {
        try {
            return Utdid.getInstance().getUtdid(context);
        } catch (Exception var2) {
            return "";
        }
    }

    public static boolean isRoot() {
        return RootUtil.isDeviceRooted();
    }
}
