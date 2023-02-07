package com.aliyun.sls.android.network_diagnosis;

import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

import com.alibaba.netspeed.network.Diagnosis;
import com.alibaba.netspeed.network.DnsConfig;
import com.alibaba.netspeed.network.HttpConfig;
import com.alibaba.netspeed.network.Logger;
import com.alibaba.netspeed.network.MtrConfig;
import com.alibaba.netspeed.network.PingConfig;
import com.alibaba.netspeed.network.TcpPingConfig;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Pair;
import com.aliyun.sls.android.core.SLSLog;
import com.aliyun.sls.android.core.configuration.Configuration;
import com.aliyun.sls.android.core.configuration.Credentials;
import com.aliyun.sls.android.core.configuration.Credentials.NetworkDiagnosisCredentials;
import com.aliyun.sls.android.core.feature.SdkFeature;
import com.aliyun.sls.android.core.sender.SdkSender;
import com.aliyun.sls.android.core.sender.Sender;
import com.aliyun.sls.android.core.utdid.Utdid;
import com.aliyun.sls.android.core.utils.AppUtils;
import com.aliyun.sls.android.network_diagnosis.INetworkDiagnosis.Callback;
import com.aliyun.sls.android.ot.Attribute;
import com.aliyun.sls.android.ot.ISpanProcessor;
import com.aliyun.sls.android.ot.SpanBuilder;
import com.aliyun.sls.android.producer.LogProducerConfig;
import com.aliyun.sls.android.producer.internal.HttpHeader;
import com.aliyun.sls.android.producer.internal.LogProducerHttpHeaderInjector;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author gordon
 * @date 2022/7/22
 */
@SuppressWarnings("unused")
public class NetworkDiagnosisFeature extends SdkFeature implements INetworkDiagnosis {
    private static final String TAG = "NetworkDiagnosisFeature";

    public static final int DEFAULT_PING_SIZE = 64;
    public static final int DEFAULT_TIMEOUT = 2 * 1000;
    public static final int DEFAULT_MAX_TIMES = 10;

    public static final int DEFAULT_MTR_MAX_TTL = 30;
    public static final int DEFAULT_MTR_MAX_PATH = 1;

    public static final String DNS_TYPE_IPv4 = "A";
    public static final String DNS_TYPE_IPv6 = "AAAA";

    private static final TaskIdGenerator TASK_ID_GENERATOR = new TaskIdGenerator();
    private NetworkDiagnosisSender networkDiagnosisSender;
    private boolean enableMultiplePortsDetect = false;

    @Override
    public String name() {
        return "network_diagnosis";
    }

    @Override
    public String version() {
        return BuildConfig.VERSION_NAME;
    }

    @Override
    public SpanBuilder newSpanBuilder(String spanName) {
        return new SpanBuilder(spanName, networkDiagnosisSender, configuration.spanProvider);
    }

    // region init
    @Override
    protected void onInitSender(Context context, Credentials credentials, Configuration configuration) {
        super.onInitSender(context, credentials, configuration);
    }

    private String getIPAIdBySecretKey(String secretKey) {
        @SuppressWarnings("CharsetObjectCanBeUsed")
        String decode = new String(
            Base64.decode(
                secretKey.getBytes(Charset.forName("UTF-8")),
                Base64.DEFAULT
            ),
            Charset.forName("UTF-8")
        );

        try {
            JSONObject object = new JSONObject(decode);
            return object.optString("ipa_app_id").toLowerCase();
        } catch (JSONException e) {
            return "";
        }
    }

    @Override
    protected void onInitialize(Context context, Credentials credentials, Configuration configuration) {
        final NetworkDiagnosisCredentials networkDiagnosisCredentials = credentials.networkDiagnosisCredentials;
        if (null == networkDiagnosisCredentials) {
            SLSLog.w(TAG, "NetworkDiagnosisCredentials must not be null.");
            return;
        }

        if (!TextUtils.isEmpty(networkDiagnosisCredentials.secretKey)) {
            networkDiagnosisCredentials.instanceId = getIPAIdBySecretKey(networkDiagnosisCredentials.secretKey);
        }

        Diagnosis.init(
            networkDiagnosisCredentials.secretKey,
            Utdid.getInstance().getUtdid(context),
            networkDiagnosisCredentials.siteId,
            networkDiagnosisCredentials.extension
        );
        Diagnosis.enableDebug(configuration.debuggable && AppUtils.debuggable(context));

        networkDiagnosisSender = new NetworkDiagnosisSender(context, this);
        networkDiagnosisSender.initialize(credentials);

        Diagnosis.registerLogger(this, networkDiagnosisSender);

        NetworkDiagnosis.getInstance().setNetworkDiagnosis(this);
    }

    @Override
    protected void onPostInitialize(Context context) {

    }

    @Override
    protected void onStop(Context context) {

    }

    @Override
    protected void onPostStop(Context context) {

    }
    // endregion

    // region setter
    @Override
    public void setCredentials(Credentials credentials) {
        super.setCredentials(credentials);
        if (null == networkDiagnosisSender) {
            return;
        }

        networkDiagnosisSender.setCredentials(credentials);
    }

    @Override
    public void setPolicyDomain(String domain) {
        if (TextUtils.isEmpty(domain)) {
            return;
        }

        Diagnosis.setPolicyDomain(domain);
    }

    @Override
    public void disableExNetworkInfo() {
        Diagnosis.disableExNetworkInfo();
    }

    @Override
    public void setMultiplePortsDetect(boolean enable) {
        this.enableMultiplePortsDetect = enable;
    }

    @Override
    public void registerCallback(Callback callback) {
        if (null != networkDiagnosisSender) {
            networkDiagnosisSender.registerGlobalCallback(callback);
        }
    }

    @Override
    public void updateExtensions(Map<String, String> extension) {
        if (null == extension) {
            return;
        }

        final Map<String, String> extensionCopy = new HashMap<>(extension);
        Diagnosis.updateExtension(extensionCopy);
    }

    @Override
    public void setCallback(Sender.Callback callback) {
        super.setCallback(callback);
        if (null != networkDiagnosisSender) {
            networkDiagnosisSender.setCallback(callback);
        }
    }

    // endregion

    // region http
    @Override
    public void http(String url) {
        this.http(url, null);
    }

    public void http(String url, Callback callback) {
        final HttpConfig config = new HttpConfig(
            TASK_ID_GENERATOR.generate(),
            url,
            (context, result) -> {
                if (null != callback) {
                    callback.onComplete(Type.HTTP, result);
                }
                return 0;
            },
            this
        );
        config.setMultiplePortsDetect(enableMultiplePortsDetect);
        Diagnosis.startHttpPing(config);
    }
    // endregion

    // region ping
    @Override
    public void ping(String domain) {
        this.ping(domain, null);
    }

    @Override
    public void ping(String domain, Callback callback) {
        this.ping(domain, DEFAULT_PING_SIZE, callback);
    }

    @Override
    public void ping(String domain, int size, Callback callback) {
        this.ping(domain, size, DEFAULT_MAX_TIMES, DEFAULT_TIMEOUT, callback);
    }

    @Override
    public void ping(String domain, int maxTimes, int timeout, Callback callback) {
        this.ping(domain, DEFAULT_PING_SIZE, maxTimes, timeout, callback);
    }

    @Override
    public void ping(String domain, int size, int maxTimes, int timeout, Callback callback) {
        final PingConfig config = new PingConfig(
            TASK_ID_GENERATOR.generate(),
            domain,
            size,
            maxTimes,
            timeout,
            (context, result) -> {
                if (null != callback) {
                    callback.onComplete(Type.PING, result);
                }
                return 0;
            },
            this
        );

        config.setMultiplePortsDetect(enableMultiplePortsDetect);
        Diagnosis.startPing(config);
    }
    // endregion

    // region tcp ping
    @Override
    public void tcpPing(String domain, int port) {
        this.tcpPing(domain, port, null);
    }

    @Override
    public void tcpPing(String domain, int port, Callback callback) {
        this.tcpPing(domain, port, DEFAULT_MAX_TIMES, callback);
    }

    @Override
    public void tcpPing(String domain, int port, int maxTimes, Callback callback) {
        this.tcpPing(domain, port, maxTimes, DEFAULT_TIMEOUT, callback);
    }

    @Override
    public void tcpPing(String domain, int port, int maxTimes, int timeout, Callback callback) {
        final TcpPingConfig config = new TcpPingConfig(
            TASK_ID_GENERATOR.generate(),
            domain,
            port,
            maxTimes,
            timeout,
            (context, result) -> {
                if (null != callback) {
                    callback.onComplete(Type.TCPPING, result);
                }
                return 0;
            },
            this
        );
        config.setMultiplePortsDetect(enableMultiplePortsDetect);
        Diagnosis.startTcpPing(config);
    }
    // endregion

    // region mtr
    @Override
    public void mtr(String domain) {
        this.mtr(domain, null);
    }

    @Override
    public void mtr(String domain, Callback callback) {
        this.mtr(domain, DEFAULT_MTR_MAX_TTL, callback);
    }

    @Override
    public void mtr(String domain, int maxTTL, Callback callback) {
        this.mtr(domain, maxTTL, DEFAULT_MTR_MAX_PATH, callback);
    }

    @Override
    public void mtr(String domain, int maxTTL, int maxPaths, Callback callback) {
        this.mtr(domain, maxTTL, maxPaths, DEFAULT_MAX_TIMES, callback);
    }

    @Override
    public void mtr(String domain, int maxTTL, int maxPaths, int maxTimes, Callback callback) {
        this.mtr(domain, maxTTL, maxPaths, maxTimes, DEFAULT_TIMEOUT, callback);
    }

    @Override
    public void mtr(String domain, int maxTTL, int maxPaths, int maxTimes, int timeout, Callback callback) {
        final MtrConfig config = new MtrConfig(
            TASK_ID_GENERATOR.generate(),
            domain,
            maxTTL,
            maxPaths,
            maxTimes,
            timeout,
            (context, result) -> {
                if (null != callback) {
                    callback.onComplete(Type.MTR, result);
                }
                return 0;
            },
            this
        );
        config.setMultiplePortsDetect(enableMultiplePortsDetect);
        Diagnosis.startMtr(config);
    }
    // endregion

    // region dns
    @Override
    public void dns(String domain) {
        this.dns(domain, null);
    }

    @Override
    public void dns(String domain, Callback callback) {
        this.dns(null, domain, callback);
    }

    @Override
    public void dns(String nameServer, String domain, Callback callback) {
        this.dns(nameServer, domain, DNS_TYPE_IPv4, callback);
    }

    @Override
    public void dns(String nameServer, String domain, String type, Callback callback) {
        this.dns(nameServer, domain, type, DEFAULT_TIMEOUT, callback);
    }

    @Override
    public void dns(String nameServer, String domain, String type, int timeout, Callback callback) {
        final DnsConfig config = new DnsConfig(
            TASK_ID_GENERATOR.generate(),
            nameServer,
            domain,
            type,
            timeout,
            (context, result) -> {
                if (null != callback) {
                    callback.onComplete(Type.DNS, result);
                }
                return 0;
            },
            this
        );
        config.setMultiplePortsDetect(enableMultiplePortsDetect);
        Diagnosis.startDns(config);
    }
    // endregion

    private static class NetworkDiagnosisSender extends SdkSender implements Logger, ISpanProcessor {

        private final SdkFeature feature;
        private INetworkDiagnosis.Callback callback;

        public NetworkDiagnosisSender(Context context, SdkFeature feature) {
            super(context);
            TAG = "NetworkDiagnosisSender";
            this.feature = feature;
        }

        protected void registerGlobalCallback(INetworkDiagnosis.Callback callback) {
            this.callback = callback;
        }

        @Override
        protected String provideFeatureName() {
            return feature.name();
        }

        @Override
        protected String provideLogFileName() {
            return "net_d";
        }

        @Override
        protected String provideEndpoint(Credentials credentials) {
            NetworkDiagnosisCredentials diagnosisCredentials = credentials.networkDiagnosisCredentials;
            if (null != diagnosisCredentials && !TextUtils.isEmpty(diagnosisCredentials.endpoint)) {
                return super.provideEndpoint(diagnosisCredentials);
            }

            return super.provideEndpoint(credentials);
        }

        @Override
        protected String provideProjectName(Credentials credentials) {
            NetworkDiagnosisCredentials diagnosisCredentials = credentials.networkDiagnosisCredentials;
            if (null != diagnosisCredentials && !TextUtils.isEmpty(diagnosisCredentials.project)) {
                return super.provideProjectName(diagnosisCredentials);
            }

            return super.provideProjectName(credentials);
        }

        @Override
        protected String provideLogstoreName(Credentials credentials) {
            if (null == credentials.networkDiagnosisCredentials) {
                return null;
            }

            return String.format("ipa-%s-raw", credentials.networkDiagnosisCredentials.instanceId);
        }

        @Override
        protected String provideAccessKeyId(Credentials credentials) {
            NetworkDiagnosisCredentials diagnosisCredentials = credentials.networkDiagnosisCredentials;
            if (null != diagnosisCredentials && !TextUtils.isEmpty(diagnosisCredentials.accessKeyId)) {
                return super.provideAccessKeyId(diagnosisCredentials);
            }

            return super.provideAccessKeyId(credentials);
        }

        @Override
        protected String provideAccessKeySecret(Credentials credentials) {
            NetworkDiagnosisCredentials diagnosisCredentials = credentials.networkDiagnosisCredentials;
            if (null != diagnosisCredentials && !TextUtils.isEmpty(diagnosisCredentials.accessKeySecret)) {
                return super.provideAccessKeySecret(diagnosisCredentials);
            }

            return super.provideAccessKeySecret(credentials);
        }

        @Override
        protected String provideSecurityToken(Credentials credentials) {
            NetworkDiagnosisCredentials diagnosisCredentials = credentials.networkDiagnosisCredentials;
            if (null != diagnosisCredentials && !TextUtils.isEmpty(diagnosisCredentials.securityToken)) {
                return super.provideSecurityToken(diagnosisCredentials);
            }

            return super.provideSecurityToken(credentials);
        }

        @Override
        protected void provideLogProducerConfig(LogProducerConfig config) {
            super.provideLogProducerConfig(config);
            config.setHttpHeaderInjector(new LogProducerHttpHeaderInjector() {
                @Override
                public String[] injectHeaders(String[] srcHeaders, int count) {
                    return HttpHeader.getHeadersWithUA(srcHeaders, String.format("%s/%s", feature.name(), feature.version()));
                }
            });
        }

        @Override
        public void report(Object context, String msg) {
            if (TextUtils.isEmpty(msg)) {
                SLSLog.w(TAG, "msg is empty.");
                return;
            }

            JSONObject object;
            try {
                object = new JSONObject(msg);
            } catch (JSONException e) {
                SLSLog.w(TAG, "msg to json error. e: " + e.getMessage());
                return;
            }

            final String method = object.optString("method");
            if (TextUtils.isEmpty(method)) {
                SLSLog.w(TAG, "method is empty.");
                return;
            }

            SLSLog.v(TAG, "network diagnosis result: method=" + method + ", result: " + msg);

            SpanBuilder builder = feature.newSpanBuilder("network_diagnosis");
            builder.addAttribute(
                Attribute.of(
                    Pair.create("t", "net_d"),
                    Pair.create("net.type", method),
                    Pair.create("net.origin", msg)
                )
            );
            builder.build().end();

            if (null != callback) {
                callback.onComplete(Type.of(method), msg);
            }
        }

        @Override
        public void debug(String tag, String msg) {
            SLSLog.d(tag, msg);
        }

        @Override
        public void info(String tag, String msg) {
            SLSLog.i(tag, msg);
        }

        @Override
        public void warm(String tag, String msg) {
            SLSLog.w(tag, msg);
        }

        @Override
        public void error(String tag, String msg) {
            SLSLog.e(tag, msg);
        }
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
}
