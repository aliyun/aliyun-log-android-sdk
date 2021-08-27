package com.aliyun.sls.android.plugin.network_monitor;

//import com.alibaba.netspeed.network.DetectCallback;
//import com.alibaba.netspeed.network.Diagnosis;
//import com.alibaba.netspeed.network.PingConfig;

import android.annotation.SuppressLint;

import com.alibaba.netspeed.network.Diagnosis;
import com.alibaba.netspeed.network.MtrConfig;
import com.alibaba.netspeed.network.PingConfig;
import com.alibaba.netspeed.network.TcpPingConfig;
import com.aliyun.sls.android.SLSConfig;
import com.aliyun.sls.android.SLSLog;
import com.aliyun.sls.android.plugin.ISender;
import com.aliyun.sls.android.scheme.Scheme;

/**
 * @author gordon
 * @date 2021/08/26
 */
public class SLSNetwork {
    private static final String TAG = "SLSNetwork";

    private ISender sender;
    private SLSConfig config;
    private TaskIdGenerator taskIdGenerator = new TaskIdGenerator();

    private static class Holder {
        private static SLSNetwork INSTANCE = new SLSNetwork();
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

    public static SLSNetwork getInstance() {
        return Holder.INSTANCE;
    }

    void init(SLSConfig config, ISender sender) {
        this.config = config;
        this.sender = sender;
    }

    private SLSNetwork() {
        //no instance
    }

    private void report(String result, Callback callback) {
        SLSLog.e(TAG, "diagnosis, result: " + result);
        Scheme scheme = Scheme.createDefaultScheme(config);
        scheme.reserve6 = result;
        sender.send(scheme);

        if (null != callback) {
            callback.onComplete(result);
        }
    }

    public void ping(String domain) {
        this.ping(domain, null);
    }

    public void ping(String domain, Callback callback) {
        this.ping(domain, 10, 1 * 1000, callback);
    }

    public void ping(String domain, int maxTimes, int timeout, Callback callback) {
        Diagnosis.startPing(new PingConfig(taskIdGenerator.generate(), domain, maxTimes, timeout, (context, result) -> {
            report(result, callback);
            return 0;
        }, this));
    }


    public void tcpPing(String domain, int port) {
        this.tcpPing(domain, port, null);
    }

    public void tcpPing(String domain, int port, Callback callback) {
        this.tcpPing(domain, port, 10, 1 * 1000, callback);
    }

    public void tcpPing(String domain, int port, int maxTimes, int timeout, Callback callback) {
        Diagnosis.startTcpPing(new TcpPingConfig(taskIdGenerator.generate(), domain, port, maxTimes, timeout, (context, result) -> {
            report(result, callback);
            return 0;
        }, this));
    }

    public void mtr(String domain) {
        this.mtr(domain, null);
    }

    public void mtr(String domain, Callback callback) {
        this.mtr(domain, 30, 1, 10, 20 * 1000, callback);
    }

    public void mtr(String domain, int maxTtl, int maxPath, int maxTimes, int timeout, Callback callback) {
        Diagnosis.startMtr(new MtrConfig(taskIdGenerator.generate(), domain, maxTtl, maxPath, maxTimes, timeout, (context, result) -> {
            report(result, callback);
            return 0;
        }, this));
    }

    public void testPing() {
        Diagnosis.startPing(new PingConfig("123456", "www.aliyun.com", 10, 3000, (context, result) -> {
            SLSLog.e(TAG, "diagnosis, result: " + result);
            Scheme scheme = Scheme.createDefaultScheme(config);
            scheme.reserve6 = result;
            sender.send(scheme);
            return 0;
        }, this));
    }

    public void testTcpPing() {
        Diagnosis.startTcpPing(new TcpPingConfig("123456", "www.aliyun.com", 80, 10, 3000, (context, result) -> {
            SLSLog.e(TAG, "diagnosis, result: " + result);
            Scheme scheme = Scheme.createDefaultScheme(config);
            scheme.reserve6 = result;
            sender.send(scheme);
            return 0;
        }, this));
    }

    public void testMtr() {
        Diagnosis.startMtr(new MtrConfig("123456", "www.aliyun.com", 30, 1, 10, 3000, (context, result) -> {
            SLSLog.e(TAG, "diagnosis, result: " + result);
            Scheme scheme = Scheme.createDefaultScheme(config);
            scheme.reserve6 = result;
            sender.send(scheme);
            return 0;
        }, this));
    }

}
