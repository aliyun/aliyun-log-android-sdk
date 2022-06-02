package com.aliyun.sls.android.scheme;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import android.content.Context;
import android.os.Build;
import android.os.Build.VERSION;
import android.text.TextUtils;
import com.aliyun.sls.android.SLSConfig;
import com.aliyun.sls.android.producer.utils.TimeUtils;

/**
 * Define the scheme of collection data.
 *
 * @author gordon
 * @date 2021/04/19
 */
@SuppressWarnings("ALL")
public class Scheme {
    public String app_id;
    public String app_name;
    public String app_version;
    public String sdk_version;
    public String sdk_type;
    public String channel;
    public String channel_name;
    public String user_nick;
    public String user_id;
    public String long_login_nick;
    public String long_login_user_id;
    public String logon_type;
    public String utdid;
    public String imei;
    public String imsi;
    public String imeisi;
    public String idfa;
    public String brand;
    public String device_model;
    public String resolution;
    public String os;
    public String os_version;
    public String carrier;
    public String access;
    public String access_subtype;
    public String network_type;
    public String school;
    public String root;
    public String reserve1;
    public String reserve2;
    public String reserve3;
    public String reserve4;
    public String reserve5;
    public String reserve6;
    public String reserves;
    public String local_time;
    public String local_timestamp;
    public String local_time_fixed;
    public String local_timestamp_fixed;
    public String reach_time;
    public String reach_time_stamp;
    public String page;
    public String event_id;
    public String event_type;
    public String arg1;
    public String arg2;
    public String arg3;
    public String args;
    public String is_active;
    public String start_count;
    public String run_time;
    public String active_uvmid;
    public String active_user_nick;
    public String page_stay_time;
    public String client_ip;
    public String country;
    public String province;
    public String city;
    public String district;
    public final Map<String, String> ext = new LinkedHashMap<>();
    public String traceId;
    public String spanId;

    public Map<String, String> toMap() {
        return toMap(false);
    }

    public Map<String, String> toMap(boolean ignoreExt) {
        Map<String, String> fields = new LinkedHashMap<>();

        putIfNotNull(fields, "app_id", app_id);
        putIfNotNull(fields, "app_name", app_name);
        putIfNotNull(fields, "app_version", app_version);
        putIfNotNull(fields, "sdk_version", sdk_version);
        putIfNotNull(fields, "sdk_type", sdk_type);
        putIfNotNull(fields, "channel", channel);
        putIfNotNull(fields, "channel_name", channel_name);
        putIfNotNull(fields, "user_nick", user_nick);
        putIfNotNull(fields, "long_login_nick", long_login_nick);
        putIfNotNull(fields, "logon_type", logon_type);
        putIfNotNull(fields, "user_id", user_id);
        putIfNotNull(fields, "long_login_user_id", long_login_user_id);
        putIfNotNull(fields, "utdid", utdid);
        putIfNotNull(fields, "imei", imei);
        putIfNotNull(fields, "imsi", imsi);
        putIfNotNull(fields, "imeisi", imeisi);
        putIfNotNull(fields, "idfa", idfa);
        putIfNotNull(fields, "brand", brand);
        putIfNotNull(fields, "device_model", device_model);
        putIfNotNull(fields, "resolution", resolution);
        putIfNotNull(fields, "os", os);
        putIfNotNull(fields, "os_version", os_version);
        putIfNotNull(fields, "carrier", carrier);
        putIfNotNull(fields, "access", access);
        putIfNotNull(fields, "access_subtype", access_subtype);
        putIfNotNull(fields, "network_type", network_type);
        putIfNotNull(fields, "school", school);
        putIfNotNull(fields, "root", root);
        putIfNotNull(fields, "reserve1", reserve1);
        putIfNotNull(fields, "reserve2", reserve2);
        putIfNotNull(fields, "reserve3", reserve3);
        putIfNotNull(fields, "reserve4", reserve4);
        putIfNotNull(fields, "reserve5", reserve5);
        putIfNotNull(fields, "reserve6", reserve6);
        putIfNotNull(fields, "reserves", reserves);
        putIfNotNull(fields, "local_time", local_time);
        putIfNotNull(fields, "local_timestamp", local_timestamp);
        putIfNotNull(fields, "local_time_fixed", local_time_fixed);
        putIfNotNull(fields, "local_timestamp_fixed", local_timestamp_fixed);
        putIfNotNull(fields, "reach_time", reach_time);
        putIfNotNull(fields, "reach_time_stamp", reach_time_stamp);
        putIfNotNull(fields, "page", page);
        putIfNotNull(fields, "event_id", event_id);
        putIfNotNull(fields, "event_type", event_type);
        putIfNotNull(fields, "arg1", arg1);
        putIfNotNull(fields, "arg2", arg2);
        putIfNotNull(fields, "arg3", arg3);
        putIfNotNull(fields, "args", args);
        putIfNotNull(fields, "is_active", is_active);
        putIfNotNull(fields, "start_count", start_count);
        putIfNotNull(fields, "run_time", run_time);
        putIfNotNull(fields, "active_uvmid", active_uvmid);
        putIfNotNull(fields, "active_user_nick", active_user_nick);
        putIfNotNull(fields, "page_stay_time", page_stay_time);
        putIfNotNull(fields, "client_ip", client_ip);
        putIfNotNull(fields, "country", country);
        putIfNotNull(fields, "province", country);
        putIfNotNull(fields, "city", city);
        putIfNotNull(fields, "district", district);
        putIfNotNull(fields, "traceId", traceId);
        putIfNotNull(fields, "spanId", spanId);

        if (ignoreExt) {
            return fields;
        }

        if (!ext.isEmpty()) {
            for (Entry<String, String> entry : ext.entrySet()) {
                put(fields, entry.getKey(), entry.getValue());
            }
        }

        return fields;
    }


    public static String returnDashIfNull(String value) {
        if (TextUtils.isEmpty(value)) {
            return "-";
        }

        return value;
    }

    private static void putIfNotNull(Map<String, String> maps, String key, String value) {
        if(TextUtils.isEmpty(value)) {
            return;
        }

        maps.put(key, value);
    }

    private static void putWithDashIfNull(Map<String, String> maps, String key, String value) {
        maps.put(key, returnDashIfNull(value));
    }

    private static void put(Map<String, String> maps, String key, String value) {
        if (null == key) {
            key = "null";
        }

        if (null == value) {
            value = "null";
        }

        maps.put(key, value);
    }

    public static Scheme createDefaultScheme(Context context) {
        Scheme scheme = new Scheme();
        Date date = new Date();
        final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS", Locale.getDefault());
        scheme.local_timestamp = String.valueOf(date.getTime());
        scheme.local_time = dateFormat.format(date);
        scheme.local_timestamp_fixed = String.valueOf(TimeUtils.getTimeInMillis() + scheme.local_timestamp.substring(10));
        date.setTime(Long.valueOf(scheme.local_timestamp_fixed));
        scheme.local_time_fixed = dateFormat.format(date);

        scheme.app_name = returnDashIfNull(AppUtils.getAppName(context));
        scheme.app_version = returnDashIfNull(AppUtils.getAppVersion(context));
        scheme.utdid = returnDashIfNull(DeviceUtils.getUtdid(context));
        scheme.imei = returnDashIfNull(DeviceUtils.getImei(context));
        scheme.imsi = returnDashIfNull(DeviceUtils.getImsi(context));
        scheme.brand = returnDashIfNull(Build.BRAND);
        scheme.device_model = returnDashIfNull(Build.MODEL);
        scheme.os = "Android";
        scheme.os_version = returnDashIfNull(VERSION.RELEASE);
        scheme.carrier = returnDashIfNull(DeviceUtils.getCarrier(context));
        scheme.access = returnDashIfNull(DeviceUtils.getAccessName(context));
        scheme.access_subtype = returnDashIfNull(DeviceUtils.getAccessSubTypeName(context));
        scheme.root = returnDashIfNull(RootUtil.isDeviceRooted() + "");
        scheme.resolution = returnDashIfNull(DeviceUtils.getResolution(context));

        return scheme;
    }

    public static Scheme createDefaultScheme(SLSConfig config) {
        Scheme scheme = createDefaultScheme(config.context);

        scheme.app_id = String.format("%s@Android", config.pluginAppId);
        scheme.channel = returnDashIfNull(config.channel);
        scheme.channel_name = returnDashIfNull(config.channelName);
        scheme.user_nick = returnDashIfNull(config.userNick);
        scheme.long_login_nick = returnDashIfNull(config.longLoginNick);
        scheme.user_id = returnDashIfNull(config.userId);
        scheme.long_login_user_id = returnDashIfNull(config.longLoginUserId);
        scheme.logon_type = returnDashIfNull(config.loginType);
        if (null != config.getExt()) {
            scheme.ext.putAll(config.getExt());
        }

        return scheme;
    }
}
