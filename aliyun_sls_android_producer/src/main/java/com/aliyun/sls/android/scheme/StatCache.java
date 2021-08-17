package com.aliyun.sls.android.scheme;

import android.content.Context;

/**
 * Cache class for store device info on runtime.
 *
 * @author gordon
 * @date 2021/04/20
 */
public class StatCache {
    public static App app = new App();
    public static Device device = new Device();

    public static void init(Context context) {
        app.packageName = context.getPackageName();
        app.appName = AppUtils.getAppName(context);
        app.appVersion = AppUtils.getAppVersion(context);

        device.utdid = DeviceUtils.getUtdid(context);
        device.imei = DeviceUtils.getImei(context);
        device.imsi = DeviceUtils.getImsi(context);
        device.carrier = DeviceUtils.getCarrier(context);
        device.accessName = DeviceUtils.getAccessName(context);
        device.accessSubTypeName = DeviceUtils.getAccessSubTypeName(context);
        device.networkType = device.accessName;
        device.root = RootUtil.isDeviceRooted();
        device.resolution = DeviceUtils.getResolution(context);
    }

    public static class App {
        public String packageName;
        public String appName;
        public String appVersion;
    }

    public static class Device {
        public String utdid;
        public String imei;
        public String imsi;
        public String carrier;
        public String accessName;
        public String accessSubTypeName;
        public String networkType;
        public boolean root;
        public String resolution;
    }
}
