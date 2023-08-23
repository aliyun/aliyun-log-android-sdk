package com.aliyun.sls.android.network_diagnosis;

import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

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
import androidx.annotation.VisibleForTesting;
import com.aliyun.sls.android.core.SLSLog;
import com.aliyun.sls.android.core.configuration.Configuration;
import com.aliyun.sls.android.core.configuration.Credentials;
import com.aliyun.sls.android.core.configuration.Credentials.NetworkDiagnosisCredentials;
import com.aliyun.sls.android.core.feature.SdkFeature;
import com.aliyun.sls.android.core.sender.SdkSender;
import com.aliyun.sls.android.core.sender.Sender;
import com.aliyun.sls.android.core.utdid.Utdid;
import com.aliyun.sls.android.core.utils.AppUtils;
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

    private static final TaskIdGenerator TASK_ID_GENERATOR = new TaskIdGenerator();
    private NetworkDiagnosisSender networkDiagnosisSender;
    @VisibleForTesting
    public boolean enableMultiplePortsDetect = false;

    private IDiagnosis diagnosis;
    private Utdid utdid;

    public NetworkDiagnosisFeature() {
        this.diagnosis = new NetSpeedDiagnosis();
        this.utdid = Utdid.getInstance();
    }

    @VisibleForTesting
    public void setDiagnosis(IDiagnosis diagnosis) {
        this.diagnosis = diagnosis;
    }

    @VisibleForTesting
    public void setUtdid(Utdid utdid) {
        this.utdid = utdid;
    }

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
    @VisibleForTesting
    public String getIPAIdBySecretKey(String secretKey) {
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
    protected void onPreInit(Context context, Credentials credentials, Configuration configuration) {
        final NetworkDiagnosisCredentials networkDiagnosisCredentials = credentials.networkDiagnosisCredentials;
        if (null == networkDiagnosisCredentials) {
            SLSLog.w(TAG, "NetworkDiagnosisCredentials must not be null.");
            return;
        }

        if (!TextUtils.isEmpty(networkDiagnosisCredentials.secretKey)) {
            networkDiagnosisCredentials.instanceId = getIPAIdBySecretKey(networkDiagnosisCredentials.secretKey);
        }

        diagnosis.preInit(
            networkDiagnosisCredentials.secretKey,
            utdid.getUtdid(context),
            networkDiagnosisCredentials.siteId,
            networkDiagnosisCredentials.extension
        );

        diagnosis.enableDebug(configuration.debuggable && AppUtils.debuggable(context));

        networkDiagnosisSender = new NetworkDiagnosisSender(context, this);
        networkDiagnosisSender.initialize(credentials);

        diagnosis.registerLogger(this, networkDiagnosisSender);

        NetworkDiagnosis.getInstance().setNetworkDiagnosis(this);
    }

    @Override
    protected void onInitialize(Context context, Credentials credentials, Configuration configuration) {
        final NetworkDiagnosisCredentials networkDiagnosisCredentials = credentials.networkDiagnosisCredentials;
        if (null == networkDiagnosisCredentials) {
            SLSLog.w(TAG, "NetworkDiagnosisCredentials must not be null.");
            return;
        }

        diagnosis.init(
            networkDiagnosisCredentials.secretKey,
            utdid.getUtdid(context),
            networkDiagnosisCredentials.siteId,
            networkDiagnosisCredentials.extension
        );
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

        // set instanceId to network diagnosis credentials that will be used in sender.provideLogstoreName() method.
        if (null != credentials.networkDiagnosisCredentials && !TextUtils.isEmpty(
            credentials.networkDiagnosisCredentials.secretKey)) {
            credentials.networkDiagnosisCredentials.instanceId = getIPAIdBySecretKey(
                credentials.networkDiagnosisCredentials.secretKey);
        }

        networkDiagnosisSender.setCredentials(credentials);
    }

    @Override
    public void setPolicyDomain(String domain) {
        if (TextUtils.isEmpty(domain)) {
            return;
        }

        diagnosis.setPolicyDomain(domain);
    }

    @Override
    public void disableExNetworkInfo() {
        diagnosis.disableExNetworkInfo();
    }

    @Override
    public void setMultiplePortsDetect(boolean enable) {
        this.enableMultiplePortsDetect = enable;
    }

    @Override
    public void registerCallback(Callback callback) {
        this.registerCallback(response -> {
            if (null != callback) {
                callback.onComplete(response.type, response.content);
            }
        });
    }

    @Override
    public void registerCallback(Callback2 callback) {
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
        diagnosis.updateExtension(extensionCopy);
    }

    @Override
    public void registerHttpCredentialCallback(HttpCredentialCallback callback) {
        diagnosis.registerHttpCredentialCallback((url, context) -> {
            if (null != callback) {
                HttpCredential credential = callback.getCredential(url, context);
                if (null != credential) {
                    return new com.alibaba.netspeed.network.HttpCredential(
                        credential.getSslContext(),
                        credential.getTrustManager()
                    );
                }
            }
            return null;
        });
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
    @Deprecated
    @Override
    public void http(String url) {
        this.http(url, null);
    }

    @Deprecated
    @Override
    public void http(String url, Callback callback) {
        this.http(url, callback, null);
    }

    @Override
    @Deprecated
    public void http(String url, Callback callback, HttpCredential credential) {
        HttpRequest request = new HttpRequest();
        request.domain = url;
        request.credential = credential;
        request.context = this;

        http(request, response -> {
            if (null != callback) {
                callback.onComplete(response.type, response.content);
            }
        });
    }

    @Override
    public void http(HttpRequest request) {
        this.http(request, null);
    }

    @Override
    public void http(HttpRequest request, Callback2 callback) {
        if (null == request || TextUtils.isEmpty(request.domain)) {
            if (null != callback) {
                callback.onComplete(Response.error("HttpRequest is null or domain is empty."));
            }
            return;
        }

        final HttpConfig config = new HttpConfig(
            TASK_ID_GENERATOR.generate(),
            request.domain,
            request.ip,
            request.timeout,
            request.downloadBytesLimit,
            request.headerOnly,
            null != request.credential ? new com.alibaba.netspeed.network.HttpCredential(
                request.credential.getSslContext(),
                request.credential.getTrustManager()
            ) : null,
            (context, result) -> {
                if (null != callback) {
                    Response response = new Response();
                    response.context = context;
                    response.type = Type.HTTP;
                    response.content = result;
                    callback.onComplete(response);
                }
                return 0;
            },
            request.context
        );
        config.setMultiplePortsDetect(enableMultiplePortsDetect || request.multiplePortsDetect);
        if (null != request.extension) {
            config.setDetectExtension(new HashMap<>(request.extension));
        }
        diagnosis.startHttpPing(config);
    }
    // endregion

    // region ping
    @Deprecated
    @Override
    public void ping(String domain) {
        this.ping(domain, null);
    }

    @Deprecated
    @Override
    public void ping(String domain, Callback callback) {
        this.ping(domain, DEFAULT_PING_SIZE, callback);
    }

    @Deprecated
    @Override
    public void ping(String domain, int size, Callback callback) {
        this.ping(domain, size, DEFAULT_MAX_TIMES, DEFAULT_TIMEOUT, callback);
    }

    @Deprecated
    @Override
    public void ping(String domain, int maxTimes, int timeout, Callback callback) {
        this.ping(domain, DEFAULT_PING_SIZE, maxTimes, timeout, callback);
    }

    @Override
    public void ping(String domain, int size, int maxTimes, int timeout, Callback callback) {
        PingRequest request = new PingRequest();
        request.domain = domain;
        request.size = size;
        request.maxTimes = maxTimes;
        request.timeout = timeout;
        request.context = this;

        ping(request, response -> {
            if (null != callback) {
                callback.onComplete(response.type, response.content);
            }
        });
    }

    public void ping(PingRequest pingRequest) {
        this.ping(pingRequest, null);
    }

    public void ping(PingRequest pingRequest, Callback2 callback) {
        if (null == pingRequest || TextUtils.isEmpty(pingRequest.domain)) {
            if (null != callback) {
                callback.onComplete(Response.error("PingRequest is null or domain is empty."));
            }
            return;
        }

        final PingConfig config = new PingConfig(
            TASK_ID_GENERATOR.generate(),
            pingRequest.domain,
            pingRequest.size,
            pingRequest.maxTimes,
            pingRequest.timeout,
            (context, result) -> {
                if (null != callback) {
                    callback.onComplete(Response.response(context, Type.PING, result));
                }
                return 0;
            },
            pingRequest.context
        );

        config.setMultiplePortsDetect(enableMultiplePortsDetect || pingRequest.multiplePortsDetect);
        if (null != pingRequest.extension) {
            config.setDetectExtension(new HashMap<>(pingRequest.extension));
        }
        diagnosis.startPing(config);
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
        TcpPingRequest request = new TcpPingRequest();
        request.domain = domain;
        request.port = port;
        request.maxTimes = maxTimes;
        request.timeout = timeout;
        request.context = this;

        this.tcpPing(request, response -> callback.onComplete(response.type, response.content));
    }

    @Override
    public void tcpPing(TcpPingRequest request) {
        this.tcpPing(request, null);
    }

    @Override
    public void tcpPing(TcpPingRequest request, Callback2 callback) {
        if (null == request || TextUtils.isEmpty(request.domain) || INVALID == request.port) {
            if (null != callback) {
                callback.onComplete(Response.error("TcpPingRequest is null or domain is empty or port is INVALID."));
            }
            return;
        }

        final TcpPingConfig config = new TcpPingConfig(
            TASK_ID_GENERATOR.generate(),
            request.domain,
            request.port,
            request.maxTimes,
            request.timeout,
            (context, result) -> {
                if (null != callback) {
                    callback.onComplete(Response.response(context, Type.TCPPING, result));
                }
                return 0;
            },
            request.context
        );
        config.setMultiplePortsDetect(enableMultiplePortsDetect || request.multiplePortsDetect);
        if (null != request.extension) {
            config.setDetectExtension(new HashMap<>(request.extension));
        }
        diagnosis.startTcpPing(config);
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
        MtrRequest request = new MtrRequest();
        request.domain = domain;
        request.maxTTL = maxTTL;
        request.maxPaths = maxPaths;
        request.maxTimes = maxTimes;
        request.timeout = timeout;
        request.context = this;

        this.mtr(request, response -> {
            if (null != callback) {
                callback.onComplete(response.type, response.content);
            }
        });
    }

    public void mtr(MtrRequest request) {
        this.mtr(request, null);
    }

    public void mtr(MtrRequest request, Callback2 callback) {
        if (null == request || TextUtils.isEmpty(request.domain)) {
            if (null != callback) {
                callback.onComplete(Response.error("MtrRequest is null or domain is empty."));
            }
            return;
        }

        final MtrConfig config = new MtrConfig(
            TASK_ID_GENERATOR.generate(),
            request.domain,
            request.maxTTL,
            request.maxPaths,
            request.maxTimes,
            request.timeout,
            (context, result) -> {
                if (null != callback) {
                    callback.onComplete(Response.response(context, Type.MTR, result));
                }
                return 0;
            },
            request.context
        );
        config.protocol = request.protocol.protocol;

        config.setMultiplePortsDetect(enableMultiplePortsDetect || request.multiplePortsDetect);
        if (null != request.extension) {
            config.setDetectExtension(new HashMap<>(request.extension));
        }
        diagnosis.startMtr(config);
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
        DnsRequest request = new DnsRequest();
        request.nameServer = nameServer;
        request.domain = domain;
        request.type = type;
        request.timeout = timeout;
        request.context = context;

        this.dns(request, response -> {
            if (null != callback) {
                callback.onComplete(response.type, response.content);
            }
        });
    }

    @Override
    public void dns(DnsRequest dnsRequest) {
        this.dns(dnsRequest, null);
    }

    @Override
    public void dns(DnsRequest dnsRequest, Callback2 callback) {
        if (null == dnsRequest || TextUtils.isEmpty(dnsRequest.domain)) {
            if (null != callback) {
                callback.onComplete(Response.error("DnsRequest is null or domain is empty."));
            }
            return;
        }

        final DnsConfig config = new DnsConfig(
            TASK_ID_GENERATOR.generate(),
            dnsRequest.nameServer,
            dnsRequest.domain,
            dnsRequest.type,
            dnsRequest.timeout,
            (context, result) -> {
                if (null != callback) {
                    callback.onComplete(Response.response(context, Type.DNS, result));
                }
                return 0;
            },
            dnsRequest.context
        );
        config.setMultiplePortsDetect(enableMultiplePortsDetect || dnsRequest.multiplePortsDetect);
        if (null != dnsRequest.extension) {
            config.setDetectExtension(new HashMap<>(dnsRequest.extension));
        }
        diagnosis.startDns(config);
    }
    // endregion

    @VisibleForTesting
    public static class NetworkDiagnosisSender extends SdkSender implements Logger, ISpanProcessor {

        private NetworkDiagnosisFeature feature;
        private INetworkDiagnosis.Callback2 callback;
        private NetworkDiagnosisHttpHeaderInjector httpHeaderInjector;

        @VisibleForTesting
        public NetworkDiagnosisSender(Context context) {
            super(context);
        }

        public NetworkDiagnosisSender(Context context, NetworkDiagnosisFeature feature) {
            super(context);
            TAG = "NetworkDiagnosisSender";
            this.httpHeaderInjector = new NetworkDiagnosisHttpHeaderInjector(feature);
            this.feature = feature;
        }

        @VisibleForTesting
        public void setFeature(NetworkDiagnosisFeature feature) {
            this.feature = feature;
        }

        protected void registerGlobalCallback(INetworkDiagnosis.Callback2 callback) {
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
            // instanceId must not be null or empty
            if (null == credentials.networkDiagnosisCredentials || TextUtils.isEmpty(
                credentials.networkDiagnosisCredentials.instanceId)) {
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
            config.setHttpHeaderInjector(httpHeaderInjector);
        }

        @VisibleForTesting
        public SpanBuilder createSpanBuilder(String msg, NetworkDiagnosisFeature feature) {
            if (TextUtils.isEmpty(msg)) {
                SLSLog.w(TAG, "msg is empty.");
                return null;
            }

            JSONObject object;
            try {
                object = new JSONObject(msg);
            } catch (JSONException e) {
                SLSLog.w(TAG, "msg to json error. e: " + e.getMessage());
                return null;
            }

            final String method = object.optString("method");
            if (TextUtils.isEmpty(method)) {
                SLSLog.w(TAG, "method is empty.");
                return null;
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
            return builder;
        }

        @VisibleForTesting
        public void handleCallback(Callback2 callback, Object context, String msg) {
            if (null == callback) {
                return;
            }

            String method;
            try {
                JSONObject object = new JSONObject(msg);
                method = object.optString("method");
            } catch (JSONException e) {
                SLSLog.w(TAG, "msg to json error. e: " + e.getMessage());
                return;
            }

            callback.onComplete(Response.response(context, Type.of(method), msg));
        }

        @Override
        public void report(Object context, String msg) {
            final SpanBuilder builder = createSpanBuilder(msg, feature);
            if (null == builder) {
                return;
            }

            builder.build().end();

            handleCallback(callback, context, msg);
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

    @VisibleForTesting
    public static class NetworkDiagnosisHttpHeaderInjector implements LogProducerHttpHeaderInjector {
        private final NetworkDiagnosisFeature feature;

        public NetworkDiagnosisHttpHeaderInjector(NetworkDiagnosisFeature feature) {
            this.feature = feature;
        }

        @Override
        public String[] injectHeaders(String[] srcHeaders, int count) {
            return HttpHeader.getHeadersWithUA(
                srcHeaders,
                String.format("%s/%s", feature.name(), feature.version())
            );
        }
    }
}
