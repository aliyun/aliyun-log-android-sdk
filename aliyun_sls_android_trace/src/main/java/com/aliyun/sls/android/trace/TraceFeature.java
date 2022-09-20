package com.aliyun.sls.android.trace;

import android.content.Context;
import android.text.TextUtils;
import com.aliyun.sls.android.core.SLSLog;
import com.aliyun.sls.android.core.configuration.Configuration;
import com.aliyun.sls.android.core.configuration.Credentials;
import com.aliyun.sls.android.core.feature.SdkFeature;
import com.aliyun.sls.android.core.sender.SdkSender;
import com.aliyun.sls.android.producer.Log;
import com.aliyun.sls.android.producer.LogProducerConfig;
import com.aliyun.sls.android.producer.internal.HttpHeader;
import com.aliyun.sls.android.producer.internal.LogProducerHttpHeaderInjector;

/**
 * @author gordon
 * @date 2022/9/6
 */
public class TraceFeature extends SdkFeature {
    private static final String TAG = "TraceFeature";
    private TraceSender traceSender;
    private TraceLogSender traceLogSender;

    @Override
    public String name() {
        return "trace";
    }

    @Override
    public String version() {
        return BuildConfig.VERSION_NAME;
    }

    @Override
    protected void onInitialize(Context context, Credentials credentials, Configuration configuration) {
        if (null == credentials || null == credentials.tracerCredentials) {
            SLSLog.w(TAG, "TraceCredentials must not be null.");
            return;
        }

        traceSender = new TraceSender(context, this);
        traceSender.initialize(credentials);

        if (configuration.enableTracerLog) {
            traceLogSender = new TraceLogSender(context, this);
            traceLogSender.initialize(credentials);
        }

        Tracer.spanProcessor = traceSender;
        Tracer.spanProvider = configuration.spanProvider;
        Tracer.setTraceFeature(this);
    }

    boolean addLog(Log log) {
        if (null == log) {
            return false;
        }

        if (null == traceLogSender) {
            return false;
        }

        return traceLogSender.send(log);
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

    private static class TraceSender extends SdkSender {
        private final SdkFeature feature;

        public TraceSender(Context context, SdkFeature feature) {
            super(context);
            TAG = "TraceSender";
            this.feature = feature;
        }

        @Override
        protected String provideFeatureName() {
            return feature.name();
        }

        @Override
        protected String provideLogFileName() {
            return "traces";
        }

        @Override
        protected String provideEndpoint(Credentials credentials) {
            return super.provideEndpoint(credentials.tracerCredentials);
        }

        @Override
        protected String provideProjectName(Credentials credentials) {
            return credentials.tracerCredentials.project;
        }

        @Override
        protected String provideLogstoreName(Credentials credentials) {
            return credentials.tracerCredentials.logstore;
        }

        @Override
        protected String provideAccessKeyId(Credentials credentials) {
            return credentials.tracerCredentials.accessKeyId;
        }

        @Override
        protected String provideAccessKeySecret(Credentials credentials) {
            return credentials.tracerCredentials.accessKeySecret;
        }

        @Override
        protected String provideSecurityToken(Credentials credentials) {
            return credentials.tracerCredentials.securityToken;
        }

        @Override
        protected void initLogProducer(Credentials credentials, String fileName) {
            super.initLogProducer(credentials, fileName);
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
        public void setCredentials(Credentials credentials) {
            super.setCredentials(credentials.tracerCredentials);
        }
    }

    private static class TraceLogSender extends TraceSender {

        public TraceLogSender(Context context, SdkFeature feature) {
            super(context, feature);
            TAG = "TraceLogSender";
        }

        protected String provideLogFileName() {
            return "traces_logs";
        }

        @Override
        protected String provideEndpoint(Credentials credentials) {
            return TextUtils.isEmpty(credentials.tracerCredentials.logCredentials.endpoint) ?
                super.provideEndpoint(credentials) : credentials.tracerCredentials.logCredentials.endpoint;
        }

        @Override
        protected String provideProjectName(Credentials credentials) {
            return TextUtils.isEmpty(credentials.tracerCredentials.logCredentials.project) ?
                super.provideEndpoint(credentials) : credentials.tracerCredentials.logCredentials.project;
        }

        @Override
        protected String provideLogstoreName(Credentials credentials) {
            return TextUtils.isEmpty(credentials.tracerCredentials.logCredentials.logstore) ?
                super.provideEndpoint(credentials) : credentials.tracerCredentials.logCredentials.logstore;
        }

    }
}
