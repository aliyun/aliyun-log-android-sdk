package com.aliyun.sls.android.okhttp.instrumentation;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.context.Context;
import io.opentelemetry.instrumentation.api.instrumenter.AttributesExtractor;
import io.opentelemetry.instrumentation.api.instrumenter.Instrumenter;
import io.opentelemetry.instrumentation.api.instrumenter.SpanKindExtractor;
import io.opentelemetry.instrumentation.api.instrumenter.http.HttpClientAttributesExtractor;
import io.opentelemetry.instrumentation.api.instrumenter.http.HttpClientAttributesGetter;
import io.opentelemetry.instrumentation.api.instrumenter.http.HttpClientMetrics;
import io.opentelemetry.instrumentation.api.instrumenter.http.HttpSpanNameExtractor;
import io.opentelemetry.instrumentation.api.instrumenter.http.HttpSpanStatusExtractor;
import io.opentelemetry.instrumentation.api.util.VirtualField;
import io.opentelemetry.instrumentation.okhttp.v3_0.internal.ConnectionErrorSpanInterceptor;
import io.opentelemetry.instrumentation.okhttp.v3_0.internal.TracingInterceptor;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * @author yulong.gyl
 * @date 2023/9/25
 * @noinspection unused
 */
public final class OkHttpConfiguration {
    static {
        System.setProperty("otel.semconv-stability.opt-in", "http");
    }

    private static VirtualField<Request, Context> contextsByRequest;
    public static Interceptor CONTEXT_INTERCEPTOR_OBJ;

    static {
        try {
            //noinspection rawtypes
            final Class TRACINGCALLFACTORY_CLASS = Class.forName(
                "io.opentelemetry.instrumentation.okhttp.v3_0.TracingCallFactory");
            final Field CONTEXTSBYREQUEST_FIELD = TRACINGCALLFACTORY_CLASS.getDeclaredField("contextsByRequest");
            CONTEXTSBYREQUEST_FIELD.setAccessible(true);
            //noinspection unchecked
            contextsByRequest = (VirtualField<Request, Context>)CONTEXTSBYREQUEST_FIELD.get(null);

            //noinspection rawtypes
            final Class CONTEXT_INTERCEPTOR = Class.forName(
                "io.opentelemetry.instrumentation.okhttp.v3_0.ContextInterceptor");
            //noinspection unchecked,rawtypes
            final Constructor constructor = CONTEXT_INTERCEPTOR.getDeclaredConstructor();
            constructor.setAccessible(true);
            CONTEXT_INTERCEPTOR_OBJ = (Interceptor)constructor.newInstance();
        } catch (Throwable e) {
            e.printStackTrace();
            CONTEXT_INTERCEPTOR_OBJ = null;
        }
    }

    private static OpenTelemetry telemetry;
    private static final List<AttributesExtractor<Request, Response>> ATTRIBUTES_EXTRACTORS = new ArrayList<>();
    private static final HttpHeadersAttributes httpHeadersAttributes = new HttpHeadersAttributes();

    static {
        ATTRIBUTES_EXTRACTORS.add(httpHeadersAttributes);
    }

    /**
     * @noinspection unused
     */
    public static void setOpenTelemetry(OpenTelemetry telemetry) {
        OkHttpConfiguration.telemetry = telemetry;
    }

    /**
     * @noinspection unused
     */
    public static void addAttributesExtractor(AttributesExtractor<Request, Response> attributesExtractor) {
        ATTRIBUTES_EXTRACTORS.add(attributesExtractor);
    }

    /**
     * @noinspection unused
     */
    public static void setCaptureRequestHeaders(boolean captureRequestHeaders) {
        httpHeadersAttributes.setCaptureRequestHeaders(captureRequestHeaders);
    }

    /**
     * @noinspection unused
     */
    public static void setCaptureRequestBody(boolean captureRequestBody) {
        httpHeadersAttributes.setCaptureRequestBody(captureRequestBody);
    }

    /**
     * @noinspection unused
     */
    public static void setCaptureResponseHeaders(boolean captureResponseHeaders) {
        httpHeadersAttributes.setCaptureResponseHeaders(captureResponseHeaders);
    }

    /**
     * @noinspection unused
     */
    public static void setCaptureResponseBody(boolean captureResponseBody) {
        httpHeadersAttributes.setCaptureResponseBody(captureResponseBody);
    }

    /**
     * @noinspection unused
     */
    public static Request injectNewCall(Request request) {
        if (null != contextsByRequest) {
            contextsByRequest.set(request, Context.current());
        }
        return request;
    }

    /**
     * @noinspection unused
     */
    public static void injectNewBuilder(OkHttpClient.Builder builder) {
        if (!builder.interceptors().contains(CONTEXT_INTERCEPTOR_OBJ)) {
            final Instrumenter<Request, Response> instrumenter = makeNewInstrumenter();
            if (null == instrumenter) {
                return;
            }

            builder.interceptors().add(0, CONTEXT_INTERCEPTOR_OBJ);
            builder.interceptors().add(1, new ConnectionErrorSpanInterceptor(instrumenter));

            builder.networkInterceptors().add(new TracingInterceptor(instrumenter, telemetry.getPropagators()));
        }
    }

    private static Instrumenter<Request, Response> makeNewInstrumenter() {
        if (null == telemetry) {
            return null;
        }

        return Instrumenter.<Request, Response>builder(telemetry,
                "io.opentelemetry.okhttp-3.0",
                HttpSpanNameExtractor.builder(OkHttpAttributesGetter.INSTANCE).build()
            )
            .setSpanStatusExtractor(HttpSpanStatusExtractor.create(
                (HttpClientAttributesGetter<? super Request, ? super Response>)OkHttpAttributesGetter.INSTANCE))
            .addAttributesExtractor(HttpClientAttributesExtractor.builder(OkHttpAttributesGetter.INSTANCE).build())
            .addAttributesExtractors(ATTRIBUTES_EXTRACTORS)
            .addOperationMetrics(HttpClientMetrics.get())
            .buildInstrumenter(SpanKindExtractor.alwaysClient());
    }
}
