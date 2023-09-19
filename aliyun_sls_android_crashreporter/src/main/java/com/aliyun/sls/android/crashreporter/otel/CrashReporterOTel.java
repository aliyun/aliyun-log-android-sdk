package com.aliyun.sls.android.crashreporter.otel;

import android.content.Context;
import android.os.Build;
import android.os.Build.VERSION;
import android.text.TextUtils;
import com.aliyun.sls.android.crashreporter.BuildConfig;
import com.aliyun.sls.android.exporter.otlp.OtlpSLSSpanExporter;
import com.aliyun.sls.android.otel.common.AccessKey;
import com.aliyun.sls.android.otel.common.ConfigurationManager;
import com.aliyun.sls.android.otel.common.ConfigurationManager.AccessKeyProvider;
import com.aliyun.sls.android.otel.common.ConfigurationManager.EnvironmentProvider;
import com.aliyun.sls.android.otel.common.ConfigurationManager.WorkspaceProvider;
import com.aliyun.sls.android.otel.common.Environment;
import com.aliyun.sls.android.otel.common.Workspace;
import com.aliyun.sls.android.otel.common.utdid.Utdid;
import com.aliyun.sls.android.otel.common.utils.AppUtils;
import com.aliyun.sls.android.otel.common.utils.DeviceUtils;
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
        final AccessKeyProvider accessKeyProvider = ConfigurationManager.getInstance().getAccessKeyProvider();
        final WorkspaceProvider workspaceProvider = ConfigurationManager.getInstance().getWorkspaceProvider();
        final EnvironmentProvider environmentProvider = ConfigurationManager.getInstance().getEnvironmentProvider();

        final AccessKey accessKey = null != accessKeyProvider ? accessKeyProvider.getAccessKey("uem") : null;
        final Workspace workspace = null != workspaceProvider ? workspaceProvider.getResource("uem") : null;
        final Environment environment = null != environmentProvider ? environmentProvider.getEnvironment("uem") : null;

        String utdid = null != environment ? environment.getUtdid() : null;
        if (TextUtils.isEmpty(utdid)) {
            utdid = Utdid.getInstance().getUtdid(context);
        }
        String instanceId = null != workspace ? workspace.getInstanceId() : "";
        String logstore = instanceId + "-uem-mobile-raw";

        OtlpSLSSpanExporter exporter = OtlpSLSSpanExporter.builder()
            .setScope("uem")
            .setEndpoint(null != workspace ? workspace.getEndpoint() : null)
            .setProject(null != workspace ? workspace.getProject() : null)
            .setLogstore(logstore)
            .setPersistentFlush(true)
            .setAccessKey(
                null != accessKey ? accessKey.getAccessKeyId() : null,
                null != accessKey ? accessKey.getAccessKeySecret() : null,
                null != accessKey ? accessKey.getAccessKeySecurityToken() : null
            )
            .build();

        SdkTracerProvider tracerProvider = SdkTracerProvider.builder()
            .addSpanProcessor(BatchSpanProcessor.builder(exporter).build())
            .setResource(Resource.getDefault()
                .merge(Resource.create(Attributes.builder()
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
                    // host
                    .put(ResourceAttributes.HOST_NAME, Build.HOST)
                    //.put(ResourceAttributes.HOST_TYPE, Build.TYPE)
                    .put(ResourceAttributes.HOST_ARCH,
                        Build.CPU_ABI + (TextUtils.isEmpty(Build.CPU_ABI2) ? "" : (", " + Build.CPU_ABI2))
                    )
                    // uem
                    .put("uem.data.type", "Android").put("uem.sdk.version", BuildConfig.VERSION_NAME)
                    // workspace
                    .put("workspace", null != workspace ? workspace.getInstanceId() : "")
                    // env
                    .put("deployment.environment", null != environment ? environment.getEnv() : "default").build())
                )
            )
            .build();

        sOpenTelemetrySdk = OpenTelemetrySdk.builder()
            .setTracerProvider(tracerProvider)
            .setPropagators(ContextPropagators.create(W3CTraceContextPropagator.getInstance()))
            .build();
    }

    public static SpanBuilder spanBuilder(String spanName) {
        return sOpenTelemetrySdk.getTracer("uem-crashreporter").spanBuilder(spanName);
    }

    public static SdkTracerProvider getTracerProvider() {
        return sOpenTelemetrySdk.getSdkTracerProvider();
    }
}
