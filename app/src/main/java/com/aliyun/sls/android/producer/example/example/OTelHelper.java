package com.aliyun.sls.android.producer.example.example;

import android.os.Build;
import com.aliyun.sls.android.core.utdid.Utdid;
import com.aliyun.sls.android.exporter.otlp.OtlpSLSSpanExporter;
import com.aliyun.sls.android.okhttp.instrumentation.OkHttpConfiguration;
import com.aliyun.sls.android.producer.BuildConfig;
import com.aliyun.sls.android.producer.example.utils.PreferenceUtils;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.propagation.W3CTraceContextPropagator;
import io.opentelemetry.context.propagation.ContextPropagators;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.SdkTracerProviderBuilder;
import io.opentelemetry.sdk.trace.export.BatchSpanProcessor;
import io.opentelemetry.semconv.ResourceAttributes;

/**
 * @author yulong.gyl
 * @date 2024/1/23
 */
public class OTelHelper {

    private static OtlpSLSSpanExporter exporter;

    public static void init(android.content.Context context) {
        exporter = OtlpSLSSpanExporter.builder()
            .setScope("trace")
            .setEndpoint(BuildConfig.UEM_ENDPOINT)
            .setProject(BuildConfig.UEM_PROJECT)
            .setLogstore(BuildConfig.UEM_INSTANCEID + "-traces")
            .setAccessKey(PreferenceUtils.getAccessKeyId(context), PreferenceUtils.getAccessKeySecret(context), null)
            .build();
    }

    public static OpenTelemetrySdk initV2X4Mobile(android.content.Context context) {
        SdkTracerProviderBuilder builder = SdkTracerProvider.builder()
            .addSpanProcessor(BatchSpanProcessor.builder(exporter).build())
            .setResource(io.opentelemetry.sdk.resources.Resource.create(Attributes.builder()
                .put(ResourceAttributes.SERVICE_NAME, "V2X-App")
                .put(ResourceAttributes.SERVICE_NAMESPACE, "V2X")
                .put(ResourceAttributes.SERVICE_VERSION, BuildConfig.VERSION_NAME)
                .put(ResourceAttributes.HOST_NAME, Build.HOST)
                .put(ResourceAttributes.OS_NAME, "Android")
                .put(ResourceAttributes.OS_TYPE, "Android")
                .put(ResourceAttributes.DEPLOYMENT_ENVIRONMENT, "dev")
                .put(ResourceAttributes.DEVICE_ID, Utdid.getInstance().getUtdid(context))
                .build()));

        SdkTracerProvider tracerProvider = builder.build();

        OpenTelemetrySdk sdk = OpenTelemetrySdk.builder()
            .setTracerProvider(tracerProvider)
            .setPropagators(ContextPropagators.create(W3CTraceContextPropagator.getInstance()))
            .build();

        OkHttpConfiguration.setOpenTelemetry(sdk);

        return sdk;
    }

    public static OpenTelemetrySdk initV2X4Veh(android.content.Context context) {
        SdkTracerProviderBuilder builder = SdkTracerProvider.builder()
            .addSpanProcessor(BatchSpanProcessor.builder(exporter).build())
            .setResource(io.opentelemetry.sdk.resources.Resource.create(Attributes.builder()
                .put(ResourceAttributes.SERVICE_NAME, "V2X-Veh")
                .put(ResourceAttributes.SERVICE_NAMESPACE, "V2X")
                .put(ResourceAttributes.SERVICE_VERSION, BuildConfig.VERSION_NAME)
                .put(ResourceAttributes.HOST_NAME, Build.HOST)
                .put(ResourceAttributes.OS_NAME, "Android")
                .put(ResourceAttributes.OS_TYPE, "Android")
                .put(ResourceAttributes.DEPLOYMENT_ENVIRONMENT, "dev")
                .put(ResourceAttributes.DEVICE_ID, Utdid.getInstance().getUtdid(context))
                .build()));

        SdkTracerProvider tracerProvider = builder.build();

        return OpenTelemetrySdk.builder()
            .setTracerProvider(tracerProvider)
            .setPropagators(ContextPropagators.create(W3CTraceContextPropagator.getInstance()))
            .build();
    }
}
