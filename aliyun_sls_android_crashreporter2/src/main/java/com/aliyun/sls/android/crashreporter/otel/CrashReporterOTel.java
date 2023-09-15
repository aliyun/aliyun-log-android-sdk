package com.aliyun.sls.android.crashreporter.otel;

import android.content.Context;
import android.os.Build;
import android.os.Build.VERSION;
import android.text.TextUtils;
import com.aliyun.sls.android.crashreporter.BuildConfig;
import com.aliyun.sls.android.exporter.otlp.OtlpSLSSpanExporter;
import com.aliyun.sls.android.otel.common.AccessKey;
import com.aliyun.sls.android.otel.common.utils.AppUtils;
import com.aliyun.sls.android.otel.common.Configuration;
import com.aliyun.sls.android.otel.common.ConfigurationManager;
import com.aliyun.sls.android.otel.common.ConfigurationManager.AccessKeyDelegate;
import com.aliyun.sls.android.otel.common.ConfigurationManager.ConfigurationDelegate;
import com.aliyun.sls.android.otel.common.ConfigurationManager.ResourceDelegate;
import com.aliyun.sls.android.otel.common.utils.DeviceUtils;
import com.aliyun.sls.android.otel.common.Resource;
import com.aliyun.sls.android.otel.common.utdid.Utdid;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.SpanBuilder;
import io.opentelemetry.api.trace.propagation.W3CTraceContextPropagator;
import io.opentelemetry.context.propagation.ContextPropagators;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.export.BatchSpanProcessor;
import io.opentelemetry.semconv.resource.attributes.ResourceAttributes;

/**
 * @author yulong.gyl
 * @date 2023/9/7
 */
public final class CrashReporterOTel {
    private static OpenTelemetrySdk sOpenTelemetrySdk;

    private static class Holder {
        private static final CrashReporterOTel INSTANCE = new CrashReporterOTel();
    }

    public static CrashReporterOTel getInstance() {
        return Holder.INSTANCE;
    }

    private CrashReporterOTel() {
        // no instance
    }

    public void initOtel(Context context) {
        final AccessKeyDelegate accessKeyDelegate = ConfigurationManager.getInstance().getAccessKeyDelegate();
        final ResourceDelegate resourceDelegate = ConfigurationManager.getInstance().getResourceDelegate();
        final ConfigurationDelegate configurationDelegate = ConfigurationManager.getInstance()
            .getConfigurationDelegate();

        final AccessKey accessKey = null != accessKeyDelegate
            ? accessKeyDelegate.getAccessKey("uem") : null;
        final Resource resource = null != resourceDelegate ?
            resourceDelegate.getResource("uem") : null;
        final Configuration configuration = null != configurationDelegate ?
            configurationDelegate.getConfiguration("uem") : null;
        String utdid = null != configuration ? configuration.getUtdid() : null;
        if (TextUtils.isEmpty(utdid)) {
            utdid = Utdid.getInstance().getUtdid(context);
        }
        String instanceId = null != resource ? resource.getInstanceId() : "";
        String logstore = instanceId + "-uem-mobile-raw";

        OtlpSLSSpanExporter exporter = OtlpSLSSpanExporter.builder()
            .setEndpoint(null != resource ? resource.getEndpoint() : null)
            .setProject(null != resource ? resource.getProject() : null)
            .setLogstore(logstore)
            .setAccessKey(
                null != accessKey ? accessKey.getAccessKeyId() : null,
                null != accessKey ? accessKey.getAccessKeySecret() : null,
                null != accessKey ? accessKey.getAccessKeySecurityToken() : null
            )
            .build();

        SdkTracerProvider tracerProvider = SdkTracerProvider.builder()
            .addSpanProcessor(BatchSpanProcessor.builder(exporter).build())
            .setResource(io.opentelemetry.sdk.resources.Resource.getDefault()
                .merge(io.opentelemetry.sdk.resources.Resource.create(Attributes.builder()
                    // service
                    .put(ResourceAttributes.SERVICE_NAME, "sls-android")
                    // device
                    .put(ResourceAttributes.DEVICE_ID, utdid)
                    .put(ResourceAttributes.DEVICE_MANUFACTURER, Build.MANUFACTURER)
                    .put(ResourceAttributes.DEVICE_MODEL_NAME, Build.MODEL)
                    .put(ResourceAttributes.DEVICE_MODEL_IDENTIFIER, Build.DEVICE)
                    .put("device.screen", DeviceUtils.getResolution(context))
                    // app
                    .put("app.version", AppUtils.getAppVersion(context))
                    .put("app.versionCode", AppUtils.getAppVersionCode(context))
                    .put("app.name", AppUtils.getAppName(context))
                    // os
                    .put(ResourceAttributes.OS_NAME, "Android")
                    .put(ResourceAttributes.OS_TYPE, "Linux")
                    .put(ResourceAttributes.OS_VERSION, VERSION.RELEASE)
                    .put(ResourceAttributes.OS_DESCRIPTION, Build.DISPLAY)
                    //.put(ResourceAttributes.OS_VERSION, Build.VERSION)
                    // host
                    .put(ResourceAttributes.HOST_NAME, Build.HOST)
                    //.put(ResourceAttributes.HOST_TYPE, Build.TYPE)
                    .put(ResourceAttributes.HOST_ARCH,
                        Build.CPU_ABI + (TextUtils.isEmpty(Build.CPU_ABI2) ? "" : (", " + Build.CPU_ABI2)))
                    // uem
                    .put("uem.data.type", "Android")
                    .put("uem.sdk.version", BuildConfig.VERSION_NAME)
                    // workspace
                    .put("workspace", null != resource ? resource.getInstanceId() : "")
                    // env
                    .put("deployment.environment", null != configuration ? configuration.getEnv() : "default")
                    .build())))
            .build();

        sOpenTelemetrySdk = OpenTelemetrySdk.builder()
            .setTracerProvider(tracerProvider)
            .setPropagators(ContextPropagators.create(W3CTraceContextPropagator.getInstance()))
            .build();

    }

    public static SpanBuilder spanBuilder(String spanName) {
        return sOpenTelemetrySdk.getTracer("uem-crashreporter").spanBuilder(spanName);
    }
}
