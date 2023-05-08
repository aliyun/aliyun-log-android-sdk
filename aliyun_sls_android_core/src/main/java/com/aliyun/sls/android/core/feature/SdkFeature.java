package com.aliyun.sls.android.core.feature;

import java.util.concurrent.atomic.AtomicBoolean;

import android.content.Context;
import com.aliyun.sls.android.core.configuration.Configuration;
import com.aliyun.sls.android.core.configuration.Credentials;
import com.aliyun.sls.android.ot.SpanBuilder;

/**
 * @author gordon
 * @date 2022/7/19
 */
public abstract class SdkFeature extends NoOpFeature {

    private final AtomicBoolean hasPreInit = new AtomicBoolean(false);
    private final AtomicBoolean hasInitialize = new AtomicBoolean(false);
    protected Context context;
    protected Configuration configuration;

    @Override
    public String name() {
        return "";
    }

    @Override
    public String version() {
        return "";
    }

    public SpanBuilder newSpanBuilder(String spanName) {
        return new SpanBuilder(spanName, configuration.spanProcessor, configuration.spanProvider);
    }

    @Override
    public void preInit(Context context, Credentials credentials, Configuration configuration) {
        if (hasPreInit.get()) {
            return;
        }
        this.context = null != context ? context.getApplicationContext() : null;
        this.configuration = configuration;

        onInitSender(context, credentials, configuration);
        onPreInit(context, credentials, configuration);

        hasPreInit.set(true);
    }

    @Override
    public final void initialize(Context context, Credentials credentials, Configuration configuration) {
        if (hasInitialize.get()) {
            return;
        }

        preInit(context, credentials, configuration);
        onInitialize(context, credentials, configuration);
        hasInitialize.set(true);
        onPostInitialize(context);
    }

    protected void onInitSender(Context context, Credentials credentials, Configuration configuration) {

    }

    protected abstract void onPreInit(Context context, Credentials credentials, Configuration configuration);
    protected abstract void onInitialize(Context context, Credentials credentials, Configuration configuration);

    protected abstract void onPostInitialize(Context context);

    protected abstract void onStop(Context context);

    protected abstract void onPostStop(Context context);

    @Override
    public boolean isInitialize() {
        return hasInitialize.get();
    }

    @Override
    public final void stop() {
        if (hasInitialize.get()) {

            onStop(context);
            hasInitialize.set(false);
            onPostStop(context);
        }
    }

    @Override
    public void setCredentials(Credentials credentials) {

    }
}
