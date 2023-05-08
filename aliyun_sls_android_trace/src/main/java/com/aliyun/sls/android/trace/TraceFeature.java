package com.aliyun.sls.android.trace;

import android.content.Context;
import android.text.TextUtils;
import com.aliyun.sls.android.core.SLSLog;
import com.aliyun.sls.android.core.configuration.Configuration;
import com.aliyun.sls.android.core.configuration.Credentials;
import com.aliyun.sls.android.core.configuration.Credentials.TracerCredentials.TracerLogCredentials;
import com.aliyun.sls.android.core.feature.SdkFeature;
import com.aliyun.sls.android.core.sender.SdkSender;
import com.aliyun.sls.android.core.sender.Sender.Callback;
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
    protected void onPreInit(Context context, Credentials credentials, Configuration configuration) {
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

    @Override
    protected void onInitialize(Context context, Credentials credentials, Configuration configuration) {

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

    @Override
    public void setCredentials(Credentials credentials) {
        super.setCredentials(credentials);
        if (null != traceSender) {
            traceSender.setCredentials(credentials);
        }
        if (null != traceLogSender) {
            traceLogSender.setCredentials(credentials);
        }
    }

    @Override
    public void setCallback(Callback callback) {
        super.setCallback(callback);
        if (null != traceSender) {
            traceSender.setCallback(callback);
        }
        if (null != traceLogSender) {
            traceLogSender.setCallback(callback);
        }
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
            if (null != credentials.tracerCredentials && !TextUtils.isEmpty(credentials.tracerCredentials.endpoint)) {
                return super.provideEndpoint(credentials.tracerCredentials);
            }

            return super.provideEndpoint(credentials);
        }

        @Override
        protected String provideProjectName(Credentials credentials) {
            if (null != credentials.tracerCredentials && !TextUtils.isEmpty(credentials.tracerCredentials.project)) {
                return super.provideProjectName(credentials.tracerCredentials);
            }

            return super.provideProjectName(credentials);
        }

        @Override
        protected String provideLogstoreName(Credentials credentials) {
            if (null != credentials.tracerCredentials && !TextUtils.isEmpty(credentials.tracerCredentials.instanceId)) {
                return String.format("%s-traces", credentials.tracerCredentials.instanceId);
            } else {
                if (!TextUtils.isEmpty(credentials.instanceId)) {
                    return String.format("%s-traces", credentials.instanceId);
                } else {
                    return null;
                }
            }
        }

        @Override
        protected String provideAccessKeyId(Credentials credentials) {
            if (null != credentials.tracerCredentials && !TextUtils.isEmpty(
                credentials.tracerCredentials.accessKeyId)) {
                return super.provideAccessKeyId(credentials.tracerCredentials);
            }

            return super.provideAccessKeyId(credentials);
        }

        @Override
        protected String provideAccessKeySecret(Credentials credentials) {
            if (null != credentials.tracerCredentials && !TextUtils.isEmpty(
                credentials.tracerCredentials.accessKeySecret)) {
                return super.provideAccessKeySecret(credentials.tracerCredentials);
            }

            return super.provideAccessKeySecret(credentials);
        }

        @Override
        protected String provideSecurityToken(Credentials credentials) {
            if (null != credentials.tracerCredentials && !TextUtils.isEmpty(
                credentials.tracerCredentials.securityToken)) {
                return super.provideSecurityToken(credentials.tracerCredentials);
            }

            return super.provideSecurityToken(credentials);
        }

        @Override
        protected void provideLogProducerConfig(LogProducerConfig config) {
            super.provideLogProducerConfig(config);
            config.setHttpHeaderInjector(new LogProducerHttpHeaderInjector() {
                @Override
                public String[] injectHeaders(String[] srcHeaders, int count) {
                    return HttpHeader.getHeadersWithUA(srcHeaders,
                        String.format("%s/%s", feature.name(), feature.version()));
                }
            });
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
            if (null != credentials.tracerCredentials) {
                TracerLogCredentials logsCredentials = credentials.tracerCredentials.logCredentials;
                if (null != logsCredentials && !TextUtils.isEmpty(logsCredentials.endpoint)) {
                    return logsCredentials.endpoint;
                }
            }

            return super.provideEndpoint(credentials);
        }

        @Override
        protected String provideProjectName(Credentials credentials) {
            if (null != credentials.tracerCredentials) {
                TracerLogCredentials logsCredentials = credentials.tracerCredentials.logCredentials;
                if (null != logsCredentials && !TextUtils.isEmpty(logsCredentials.project)) {
                    return logsCredentials.project;
                }
            }

            return super.provideProjectName(credentials);
        }

        @Override
        protected String provideLogstoreName(Credentials credentials) {
            if (null != credentials.tracerCredentials) {
                TracerLogCredentials logsCredentials = credentials.tracerCredentials.logCredentials;
                // use user custom logstore first
                if (null != logsCredentials && !TextUtils.isEmpty(logsCredentials.logstore)) {
                    return logsCredentials.logstore;
                }

                // then use the tracer credentials instanceId
                if (!TextUtils.isEmpty(credentials.tracerCredentials.instanceId)) {
                    return String.format("%s-logs", credentials.tracerCredentials.instanceId);
                }
            }

            // last use the root credentials instanceId
            if (!TextUtils.isEmpty(credentials.instanceId)) {
                return String.format("%s-logs", credentials.instanceId);
            }

            // or return null
            return null;
        }
    }
}
