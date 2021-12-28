package com.aliyun.sls.android.plugin.network_diagnosis;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

import com.alibaba.netspeed.network.Diagnosis;
import com.alibaba.netspeed.network.HttpConfig;
import com.alibaba.netspeed.network.MtrConfig;
import com.alibaba.netspeed.network.PingConfig;
import com.alibaba.netspeed.network.TcpPingConfig;
import com.aliyun.sls.android.JsonUtil;
import com.aliyun.sls.android.SLSConfig;
import com.aliyun.sls.android.SLSLog;
import com.aliyun.sls.android.plugin.ISender;
import com.aliyun.sls.android.scheme.Scheme;

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

    private enum Type {
        /**
         * PING
         */
        PING,
        /**
         * TCPPING
         */
        TCPPING,
        /**
         * MTR
         */
        MTR,
        /**
         * HTTP
         */
        HTTP
    }

    private ISender sender;
    private SLSConfig config;
    private final TaskIdGenerator taskIdGenerator = new TaskIdGenerator();

    private Handler handler;

    private static class Holder {
        private final static SLSNetDiagnosis INSTANCE = new SLSNetDiagnosis();
    }

    public interface Callback {
        void onComplete(String result);
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
    }

    private SLSNetDiagnosis() {
        //no instance
        handler = new Handler(Looper.getMainLooper());
    }

    private void report(Type type, String result, Callback callback) {
        SLSLog.e(TAG, "diagnosis, result: " + result);
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
        scheme.reserves = reserves.toString();

        sender.send(scheme);

        if (null != callback) {
            handler.post(() -> callback.onComplete(result));
        }
    }

    public void ping(String domain) {
        this.ping(domain, null);
    }

    public void ping(String domain, Callback callback) {
        this.ping(domain, 10, DEFAULT_TIMEOUT, callback);
    }

    public void ping(String domain, int maxTimes, int timeout, Callback callback) {
        Diagnosis.startPing(new PingConfig(taskIdGenerator.generate(), domain, maxTimes, timeout, (context, result) -> {
            report(Type.PING, result, callback);
            return 0;
        }, this));
    }


    public void tcpPing(String domain, int port) {
        this.tcpPing(domain, port, null);
    }

    public void tcpPing(String domain, int port, Callback callback) {
        this.tcpPing(domain, port, 10, DEFAULT_TIMEOUT, callback);
    }

    public void tcpPing(String domain, int port, int maxTimes, int timeout, Callback callback) {
        Diagnosis.startTcpPing(new TcpPingConfig(taskIdGenerator.generate(), domain, port, maxTimes, timeout, (context, result) -> {
            report(Type.TCPPING, result, callback);
            return 0;
        }, this));
    }

    public void mtr(String domain) {
        this.mtr(domain, null);
    }

    public void mtr(String domain, Callback callback) {
        this.mtr(domain, 30, 1, 10, DEFAULT_TIMEOUT, callback);
    }

    public void mtr(String domain, int maxTtl, int maxPath, int maxTimes, int timeout, Callback callback) {
        MtrConfig config = new MtrConfig(taskIdGenerator.generate(), domain, maxTtl, maxPath, maxTimes, timeout, (context, result) -> {
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
