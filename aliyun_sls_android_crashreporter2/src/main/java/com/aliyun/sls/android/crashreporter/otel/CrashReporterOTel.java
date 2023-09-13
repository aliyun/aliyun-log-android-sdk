package com.aliyun.sls.android.crashreporter.otel;

import android.content.Context;
import android.os.Build;
import android.os.Build.VERSION;
import android.text.TextUtils;
import com.aliyun.sls.android.crashreporter.BuildConfig;
import com.aliyun.sls.android.exporter.otlp.OtlpSLSSpanExporter;
import com.aliyun.sls.android.otel.common.AccessKeyConfiguration;
import com.aliyun.sls.android.otel.common.AppUtils;
import com.aliyun.sls.android.otel.common.ConfigurationManager;
import com.aliyun.sls.android.otel.common.ConfigurationManager.AccessKeyDelegate;
import com.aliyun.sls.android.otel.common.ConfigurationManager.ResourceDelegate;
import com.aliyun.sls.android.otel.common.DeviceUtils;
import com.aliyun.sls.android.otel.common.ResourceConfiguration;
import com.aliyun.sls.android.otel.common.utdid.Utdid;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.SpanBuilder;
import io.opentelemetry.api.trace.propagation.W3CTraceContextPropagator;
import io.opentelemetry.context.propagation.ContextPropagators;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.resources.Resource;
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

        final AccessKeyConfiguration accessKeyConfiguration = null != accessKeyDelegate
            ? accessKeyDelegate.getAccessKey("uem") : null;
        final ResourceConfiguration resourceConfiguration = null != resourceDelegate ?
            resourceDelegate.getResource("uem") : null;

        OtlpSLSSpanExporter exporter = OtlpSLSSpanExporter.builder()
            .setEndpoint(null != resourceConfiguration ? resourceConfiguration.getEndpoint() : null)
            .setProject(null != resourceConfiguration ? resourceConfiguration.getProject() : null)
            .setLogstore(null != resourceConfiguration ? resourceConfiguration.getInstanceId() : null)
            .setAccessKey(
                null != accessKeyConfiguration ? accessKeyConfiguration.getAccessKeyId() : null,
                null != accessKeyConfiguration ? accessKeyConfiguration.getAccessKeySecret() : null,
                null != accessKeyConfiguration ? accessKeyConfiguration.getAccessKeySecurityToken() : null
            )
            .build();

        SdkTracerProvider tracerProvider = SdkTracerProvider.builder()
            .addSpanProcessor(BatchSpanProcessor.builder(exporter).build())
            .setResource(Resource.getDefault()
                .merge(Resource.create(Attributes.builder()
                    // device
                    .put(ResourceAttributes.DEVICE_ID, Utdid.getInstance().getUtdid(context))
                    .put(ResourceAttributes.DEVICE_MANUFACTURER, Build.MANUFACTURER)
                    .put(ResourceAttributes.DEVICE_MODEL_NAME, Build.MODEL)
                    .put(ResourceAttributes.DEVICE_MODEL_IDENTIFIER, Build.DEVICE)
                    .put("device.resolution", DeviceUtils.getResolution(context))
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
