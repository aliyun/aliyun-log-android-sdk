package com.aliyun.sls.android.plugin.network_diagnosis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.alibaba.netspeed.network.Diagnosis;
import com.alibaba.netspeed.network.HttpConfig;
import com.alibaba.netspeed.network.Logger;
import com.alibaba.netspeed.network.MtrConfig;
import com.alibaba.netspeed.network.PingConfig;
import com.alibaba.netspeed.network.TcpPingConfig;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import com.aliyun.sls.android.JsonUtil;
import com.aliyun.sls.android.SLSConfig;
import com.aliyun.sls.android.SLSLog;
import com.aliyun.sls.android.plugin.ISender;
import com.aliyun.sls.android.plugin.network_diagnosis.SLSNetPolicy.Destination;
import com.aliyun.sls.android.producer.Log;
import com.aliyun.sls.android.scheme.Scheme;
import com.aliyun.sls.android.utdid.Utdid;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author gordon
 * @date 2021/08/26
 */
public class SLSNetDiagnosis {
    private static final String TAG = "SLSNetwork";
    /**
     * default timeout: 1 second
     */
    @SuppressWarnings("PointlessArithmeticExpression")
    private static final int DEFAULT_TIMEOUT = 1 * 1000;

    public enum Type {
        /**
         * PING
         */
        PING("PING"),
        /**
         * TCPPING
         */
        TCPPING("TCPPING"),
        /**
         * MTR
         */
        MTR("MTR"),
        /**
         * HTTP
         */
        HTTP("HTTP");

        private final static Map<String, Type> sTypeMap = new HashMap<String, Type>() {
            {
                put(PING.type, PING);
                put(TCPPING.type, TCPPING);
                put(MTR.type, MTR);
                put(HTTP.type, HTTP);
            }
        };

        public final String type;

        Type(String type) {
            this.type = type;
        }

        public static Type typeOf(String type) {
            if (TextUtils.isEmpty(type)) {
                return null;
            }

            return sTypeMap.get(type.toUpperCase());
        }
    }

    private ISender sender;
    private SLSConfig config;
    private final TaskIdGenerator taskIdGenerator = new TaskIdGenerator();

    private final Handler handler;

    private final List<Callback2> callbacks = new ArrayList<>();

    private static class Holder {
        private final static SLSNetDiagnosis INSTANCE = new SLSNetDiagnosis();
    }

    public interface Callback {
        void onComplete(String result);
    }

    public interface Callback2 {
        void onComplete2(Type type, String result);
    }

    private static class TaskIdGenerator {

        private final String prefix = String.valueOf(System.nanoTime());
        private long index = 0;

        @SuppressLint("DefaultLocale")
        synchronized String generate() {
            index += 1;
            return String.format("%s_%d", prefix, index);
        }
    }

    public static SLSNetDiagnosis getInstance() {
        return Holder.INSTANCE;
    }

    void init(SLSConfig config, ISender sender) {
        this.config = config;
        this.sender = sender;
        Diagnosis.init(config.pluginAppId, Utdid.getInstance().getUtdid(config.context), "public");
    }

    private SLSNetDiagnosis() {
        //no instance
        handler = new Handler(Looper.getMainLooper());
        Diagnosis.registerLogger(this, new Logger() {
            @Override
            public void debug(String tag, String msg) {
                if (null != config && config.debuggable) {
                    SLSLog.v(tag, "debug. tag: " + tag + ", msg: " + msg);
                }
            }

            @Override
            public void info(String tag, String msg) {
                if (null != config && config.debuggable) {
                    SLSLog.d(tag, "info. tag: " + tag + ", msg: " + msg);
                }
            }

            @Override
            public void warm(String tag, String msg) {
                SLSLog.w(tag, "warm. tag: " + tag + ", msg: " + msg);
            }

            @Override
            public void error(String tag, String msg) {
                SLSLog.e(tag, "error. tag: " + tag + ", msg: " + msg);
            }

            @Override
            public void report(Object context, String msg) {
                if (context == SLSNetDiagnosis.this) {
                    // ignore
                    return;
                }

                if (null != config && config.debuggable) {
                    SLSLog.v(TAG, "report. msg: " + msg);
                }

                if (TextUtils.isEmpty(msg)) {
                    SLSLog.w(TAG, "report msg is null");
                    return;
                }

                try {
                    JSONObject object = new JSONObject(msg);
                    final String method = object.optString("method");
                    if (TextUtils.isEmpty(method)) {
                        SLSLog.w(TAG, "report. not a valid method: " + method);
                        return;
                    }

                    final Type type = Type.typeOf(method);
                    if (null == type) {
                        SLSLog.w(TAG, "report. type is null");
                        return;
                    }

                    SLSNetDiagnosis.this.report(type, msg, result -> {
                        for (Callback2 callback : callbacks) {
                            callback.onComplete2(type, result);
                        }
                    });
                } catch (Throwable e) {
                    e.printStackTrace();
                    SLSLog.w(TAG, "report. exception: " + e.getLocalizedMessage());
                }
            }
        });
    }

    private void report(Type type, String result, Callback callback) {
        if (config.debuggable) {
            SLSLog.v(TAG, "diagnosis, result: " + result);
        }
        Scheme scheme = Scheme.createDefaultScheme(config);
        if (!TextUtils.isEmpty(scheme.app_id) && scheme.app_id.contains("@")) {
            scheme.app_id = scheme.app_id.substring(0, scheme.app_id.indexOf("@"));
        }
        scheme.reserve6 = result;

        JSONObject reserves = new JSONObject();
        if (type == Type.PING) {
            JsonUtil.putOpt(reserves, "method", "PING");
        } else if (type == Type.TCPPING) {
            JsonUtil.putOpt(reserves, "method", "TCPPING");
        } else if (type == Type.MTR) {
            JsonUtil.putOpt(reserves, "method", "MTR");
        } else if (type == Type.HTTP) {
            JsonUtil.putOpt(reserves, "method", "HTTP");
        } else {
            JsonUtil.putOpt(reserves, "method", "UNKNOWN");
        }

        // put ext fields to reserves
        if (null != config.getExt()) {
            for (Entry<String, String> entry : config.getExt().entrySet()) {
                JsonUtil.putOpt(reserves, entry.getKey(), entry.getValue());
            }
        }
        scheme.reserves = reserves.toString();

        Log log = new Log();
        // ignore ext fields
        for (Map.Entry<String, String> entry : scheme.toMap(true).entrySet()) {
            log.putContent(entry.getKey(), entry.getValue());
        }
        sender.send(log);

        if (null != callback) {
            handler.post(() -> callback.onComplete(result));
        }
    }

    public void updateConfig(SLSConfig config) {
        if (!TextUtils.isEmpty(config.channel)) {
            this.config.channel = config.channel;
        }
        if (!TextUtils.isEmpty(config.channelName)) {
            this.config.channelName = config.channelName;
        }
        if (!TextUtils.isEmpty(config.userNick)) {
            this.config.userNick = config.userNick;
        }
        if (!TextUtils.isEmpty(config.longLoginNick)) {
            this.config.longLoginNick = config.longLoginNick;
        }
        if (!TextUtils.isEmpty(config.userId)) {
            this.config.userId = config.userId;
        }
        if (!TextUtils.isEmpty(config.longLoginUserId)) {
            this.config.longLoginUserId = config.longLoginUserId;
        }
        if (!TextUtils.isEmpty(config.loginType)) {
            this.config.loginType = config.loginType;
        }
        if (null != config.getExt()) {
            this.config.setExt(config.getExt());
        }
    }

    /**
     * 注册全局回调。仅当注册了策略时，该回调才会被触发。
     *
     * @param callback
     */
    public void registerCallback(Callback2 callback) {
        this.callbacks.add(callback);
    }

    /**
     * 移除全局回调。
     * @param callback
     */
    public void removeCallback(Callback2 callback) {
        this.callbacks.remove(callback);
    }

    /**
     * 清空全局回调。
     */
    public void clearCallback() {
        this.callbacks.clear();
    }

    /**
     * 注册策略.
     * @param policy json格式的策略描述
     */
    public void registerPolicy(String policy) {
        Diagnosis.handleMessage(this, null, policy);
    }

    /**
     * 注册策略。
     *
     * @param builder {@link SLSNetPolicyBuilder}
     */
    public void registerPolicy(SLSNetPolicyBuilder builder) {
        SLSNetPolicy policy = builder.create();

        JSONObject object = new JSONObject();
        try {
            object.put("switch", policy.enable ? "on" : "off");
            object.put("type", "policy_" + policy.type);
            object.put("version", policy.version);
            object.put("periodicity", policy.periodicity);
            object.put("interval", policy.interval);
            object.put("expiration", policy.expiration);
            object.put("ratio", policy.ratio);
            object.put("whitelist", listToArray(policy.whitelist));
            object.put("methods", listToArray(policy.methods));

            JSONArray array = new JSONArray();
            for (Destination destination : policy.destination) {
                JSONObject obj = new JSONObject();
                obj.put("siteId", destination.siteId);
                //obj.put("az", destination.az);
                obj.put("ips", listToArray(destination.ips));
                obj.put("urls", listToArray(destination.urls));
                array.put(obj);
            }
            object.put("destination", array);
        } catch (JSONException e) {
            e.printStackTrace();
            SLSLog.e(TAG, "register policy error. e: " + e.getLocalizedMessage());
            return;
        }

        if (config.debuggable) {
            SLSLog.v(TAG, "registerPolicy, policy: " + object);
        }

        registerPolicy(object.toString());
    }

    private static JSONArray listToArray(List<String> list) {
        JSONArray array = new JSONArray();

        if (null == list) {
            return array;
        }

        for (String s : list) {
            array.put(s);
        }
        return array;
    }

    /**
     * @param domain 目标 host，如 www.aliyun.com
     */
    public void ping(String domain) {
        this.ping(domain, null);
    }

    /**
     * @param domain   目标 host，如 www.aliyun.com
     * @param callback 回调 callback
     */
    public void ping(String domain, Callback callback) {
        this.ping(domain, 64, callback);
    }

    /**
     * @param domain 目标host，如 www.aliyun.com
     * @param size ping包大小，如 64
     * @param callback 回调callback
     */
    public  void ping(String domain, int size, Callback callback) {
        this.ping(domain, size, 10 , DEFAULT_TIMEOUT, callback);
    }

    /**
     * @param domain   目标 host，如 www.aliyun.com
     * @param maxTimes 探测的次数
     * @param timeout  单次探测的超时时间
     * @param callback 回调 callback
     */
    public void ping(String domain, int maxTimes, int timeout, Callback callback) {
        this.ping(domain, 64, maxTimes, timeout, callback);
    }

    /**
     * @param domain 目标host，如： www.aliyun.com
     * @param size ping包大小，如：64
     * @param maxTimes 探测的次数
     * @param timeout 单次探测的超时时间
     * @param callback 回调callback
     */
    public void ping(String domain, int size, int maxTimes, int timeout, Callback callback) {
        Diagnosis.startPing(new PingConfig(taskIdGenerator.generate(), domain, size, maxTimes, timeout, (context, result) -> {
            report(Type.PING, result, callback);
            return 0;
        }, this));
    }

    /**
     * @param domain 目标 host，如：www.aliyun.com
     * @param port   目标端口，如：80
     */
    public void tcpPing(String domain, int port) {
        this.tcpPing(domain, port, null);
    }

    /**
     * @param domain   目标 host，如：www.aliyun.com
     * @param port     目标端口，如：80
     * @param callback 回调 callback
     */
    public void tcpPing(String domain, int port, Callback callback) {
        this.tcpPing(domain, port, 10, DEFAULT_TIMEOUT, callback);
    }

    /**
     * @param domain   目标 host，如：www.aliyun.com
     * @param port     目标端口，如：80
     * @param maxTimes 探测的次数
     * @param timeout  单次探测的超时时间
     * @param callback 回调 callback
     */
    public void tcpPing(String domain, int port, int maxTimes, int timeout, Callback callback) {
        Diagnosis.startTcpPing(
            new TcpPingConfig(taskIdGenerator.generate(), domain, port, maxTimes, timeout, (context, result) -> {
                report(Type.TCPPING, result, callback);
                return 0;
            }, this));
    }

    /**
     * @param domain 目标 host，如：www.aliyun.com
     */
    public void mtr(String domain) {
        this.mtr(domain, null);
    }

    /**
     * @param domain   目标 host，如 www.aliyun.com
     * @param callback 回调 callback
     */
    public void mtr(String domain, Callback callback) {
        this.mtr(domain, 30, 1, 10, DEFAULT_TIMEOUT, callback);
    }

    /**
     * @param domain   目标 host，如：www.aliyun.com
     * @param maxTtl   最大生存时间
     * @param maxPath  探测路径数量
     * @param maxTimes 探测的次数
     * @param timeout  单次探测的超时时间
     * @param callback 回调 callback
     */
    public void mtr(String domain, int maxTtl, int maxPath, int maxTimes, int timeout, Callback callback) {
        MtrConfig config = new MtrConfig(taskIdGenerator.generate(), domain, maxTtl, maxPath, maxTimes, timeout,
            (context, result) -> {
                if (!TextUtils.isEmpty(result)) {
                    try {
                        JSONArray array = new JSONArray(result);
                        final int size = array.length();
                        for (int i = 0; i < size; i++) {
                            report(Type.MTR, array.getJSONObject(i).toString(), i == size - 1 ? callback : null);
                        }
                    } catch (JSONException e) {
                        report(Type.MTR, result, callback);
                    }
                }
                return 0;
            }, this);
        config.setCombineCallback(true);
        Diagnosis.startMtr(config);
    }

    public void http(String httpUrl, Callback callback) {
        Diagnosis.startHttpPing(new HttpConfig(taskIdGenerator.generate(), httpUrl, (context, result) -> {
            report(Type.HTTP, result, callback);
            return 0;
        }, this));
    }

    public void http(String httpUrl) {
        this.http(httpUrl, null);
    }
}
