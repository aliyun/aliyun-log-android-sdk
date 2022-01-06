package com.aliyun.sls.android.plugin.trace;

import static io.opentelemetry.semconv.resource.attributes.ResourceAttributes.HOST_NAME;
import static io.opentelemetry.semconv.resource.attributes.ResourceAttributes.TELEMETRY_SDK_LANGUAGE;

import android.os.Build;
import android.text.TextUtils;

import com.aliyun.sls.android.SLSConfig;
import com.aliyun.sls.android.SLSLog;
import com.aliyun.sls.android.plugin.trace.processor.SLSAutoEndParentSpanProcessor;
import com.aliyun.sls.android.plugin.trace.processor.SLSSpanProcessor;
import com.aliyun.sls.android.utdid.Utdid;

import java.util.Arrays;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.api.trace.TracerProvider;
import io.opentelemetry.api.trace.propagation.W3CTraceContextPropagator;
import io.opentelemetry.context.propagation.ContextPropagators;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor;

/**
 * @author gordon
 * @date 2021/07/29
 */
public class SLSTelemetry {
    private static final String TAG = "SLSTelemetry";

    private static SLSTelemetry INSTANCE;
    private final OpenTelemetrySdk telemetrySdk;

    SLSTelemetry(SLSConfig config, SLSSpanExporter spanExporter) {
        //no instance
        SdkTracerProvider sdkTracerProvider = SdkTracerProvider.builder()
//                .setClock(new SLSClock())
                .addSpanProcessor(SLSSpanProcessor.create(Arrays.asList(
                        SimpleSpanProcessor.create(spanExporter)
//                        , new SLSDefaultSpanProcessor(config.context)
                        , new SLSAutoEndParentSpanProcessor()
                )))
//                .addSpanProcessor(SimpleSpanProcessor.create(new SLSSpanExporter(config)))
                .setResource(Resource.getDefault()
                        .merge(
                                Resource.create(
                                        Attributes.builder()
                                                .put(TELEMETRY_SDK_LANGUAGE, "Android")
                                                .put(HOST_NAME, "Android")
                                                .put("service.name", "Android")
                                                // device specification, ref: https://github.com/open-telemetry/opentelemetry-specification/blob/main/specification/resource/semantic_conventions/device.md
                                                .put("device.id", Utdid.getInstance().getUtdid(config.context))
                                                .put("device.model.identifier", Build.MODEL)
                                                .put("device.model.name", Build.PRODUCT)

                                                // os specification, ref: https://github.com/open-telemetry/opentelemetry-specification/blob/main/specification/resource/semantic_conventions/os.md
                                                .put("os.type", "Linux")
                                                .put("os.description", Build.DISPLAY)
                                                .put("os.name", "Android")
                                                .put("os.version", Build.VERSION.RELEASE)
                                                .put("os.sdk", Build.VERSION.SDK)

                                                // host specification, ref: https://github.com/open-telemetry/opentelemetry-specification/blob/main/specification/resource/semantic_conventions/host.md
                                                .put("host.id", Utdid.getInstance().getUtdid(config.context))
                                                .put("host.name", Build.HOST)
                                                .put("host.type", Build.TYPE)
                                                .put("host.arch", Build.CPU_ABI + (TextUtils.isEmpty(Build.CPU_ABI2) ? "" : (", " + Build.CPU_ABI2)))
                                                .put("sls.sdk.language", "Android")
                                                .put("sls.sdk.name", "tracesdk")
                                                .put("sls.sdk.version", BuildConfig.VERSION_NAME)
                                                .build()
                                )
                        ))
                .build();

        telemetrySdk = OpenTelemetrySdk.builder()
                .setTracerProvider(sdkTracerProvider)
                .setPropagators(ContextPropagators.create(W3CTraceContextPropagator.getInstance()))
                .build();
        INSTANCE = this;
    }

    public static SLSTelemetry getInstance() {
        if (null == INSTANCE) {
            SLSLog.e(TAG, "INSTANCE is null. You should init SLSTracePlugin first.");
            return null;
        }

        return INSTANCE;
    }

    public Tracer getTracer(String name) {
        return telemetrySdk.getTracer(name);
    }

    public Tracer getTracer(String name, String version) {
        return telemetrySdk.getTracer(name, version);
    }

    public ContextPropagators getPropagators() {
        return telemetrySdk.getPropagators();
    }

    public SdkTracerProvider getSdkTracerProvider() {
        return telemetrySdk.getSdkTracerProvider();
    }

    public TracerProvider getTracerProvider() {
        return telemetrySdk.getTracerProvider();
    }
}
