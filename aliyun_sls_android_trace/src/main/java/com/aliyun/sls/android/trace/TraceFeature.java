package com.aliyun.sls.android.trace;

import android.content.Context;
import com.aliyun.sls.android.core.SLSLog;
import com.aliyun.sls.android.core.configuration.Configuration;
import com.aliyun.sls.android.core.configuration.Credentials;
import com.aliyun.sls.android.core.feature.SdkFeature;
import com.aliyun.sls.android.core.sender.SdkSender;

/**
 * @author gordon
 * @date 2022/9/6
 */
public class TraceFeature extends SdkFeature {
    private static final String TAG = "TraceFeature";
    private TraceSender traceSender;

    @Override
    protected void onInitialize(Context context, Credentials credentials, Configuration configuration) {
        if (null == credentials || null == credentials.tracerCredentials) {
            SLSLog.w(TAG, "TraceCredentials must not be null.");
            return;
        }

        traceSender = new TraceSender(context, this);
        traceSender.initialize(credentials);

        Tracer.spanProcessor = traceSender;
        Tracer.spanProvider = configuration.spanProvider;
        Tracer.setTraceFeature(this);
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
        public void setCredentials(Credentials credentials) {
            super.setCredentials(credentials.tracerCredentials);
        }
    }
}
