package com.aliyun.sls.android.webview.instrumentation;

import android.os.Build;
import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.propagation.W3CTraceContextPropagator;
import io.opentelemetry.context.propagation.ContextPropagators;
import io.opentelemetry.exporter.otlp.trace.OtlpGrpcSpanExporter;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.export.BatchSpanProcessor;
import io.opentelemetry.semconv.resource.attributes.ResourceAttributes;

/**
 * @author yulong.gyl
 * @date 2023/7/7
 */
public class TelemetryTestHelper {
    private TelemetryTestHelper() {
        //no instance
    }

    public static void initTelemetry() {
        if (GlobalOpenTelemetry.get() != null) {
            return;
        }
        
        OtlpGrpcSpanExporter grpcSpanExporter = OtlpGrpcSpanExporter.builder()
            .build();

        SdkTracerProvider tracerProvider = SdkTracerProvider.builder()
            .addSpanProcessor(BatchSpanProcessor.builder(grpcSpanExporter).build())
            .setResource(io.opentelemetry.sdk.resources.Resource.create(Attributes.builder()
                .put(ResourceAttributes.SERVICE_NAME, "Android Demo App")
                .put(ResourceAttributes.SERVICE_NAMESPACE, "Android")
                .put(ResourceAttributes.SERVICE_VERSION, BuildConfig.VERSION_NAME)
                .put(ResourceAttributes.HOST_NAME, Build.HOST)
                .put(ResourceAttributes.OS_NAME, "Android")
                .put(ResourceAttributes.OS_TYPE, "Android")
                .put(ResourceAttributes.DEPLOYMENT_ENVIRONMENT, "dev")
                .put(ResourceAttributes.DEVICE_ID, "1111111")
                .build()))
            .build();

        OpenTelemetrySdk.builder()
            .setTracerProvider(tracerProvider)
            .setPropagators(ContextPropagators.create(W3CTraceContextPropagator.getInstance()))
            .buildAndRegisterGlobal();
    }

}
