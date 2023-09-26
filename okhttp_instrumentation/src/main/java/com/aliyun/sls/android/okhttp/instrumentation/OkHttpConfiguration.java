package com.aliyun.sls.android.okhttp.instrumentation;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.common.AttributesBuilder;
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
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.Buffer;
import okio.BufferedSource;

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

    private static class HttpHeadersAttributes implements AttributesExtractor<Request, Response> {
        private boolean captureRequestHeaders = true;
        private boolean captureRequestBody = false;
        private boolean captureResponseHeaders = true;
        private boolean captureResponseBody = false;

        public void setCaptureRequestHeaders(boolean captureRequestHeaders) {
            this.captureRequestHeaders = captureRequestHeaders;
        }

        public void setCaptureRequestBody(boolean captureRequestBody) {
            this.captureRequestBody = captureRequestBody;
        }

        public void setCaptureResponseHeaders(boolean captureResponseHeaders) {
            this.captureResponseHeaders = captureResponseHeaders;
        }

        public void setCaptureResponseBody(boolean captureResponseBody) {
            this.captureResponseBody = captureResponseBody;
        }

        @Override
        public void onStart(AttributesBuilder attributes, Context parentContext, Request request) {
            if (captureRequestHeaders && null != request.headers()) {
                for (String name : request.headers().names()) {
                    attributes.put("http.request.header." + name, request.header(name));
                }
            }

            if (captureRequestBody && null != request.body()) {
                String body = requestBody2String(request.body());
                if (null != body) {
                    attributes.put("http.request.body", body);
                }
            }
        }

        @Override
        public void onEnd(AttributesBuilder attributes, Context context, Request request, Response response,
            Throwable error) {
            if (captureResponseHeaders && null != response.headers()) {
                for (String name : response.headers().names()) {
                    attributes.put("http.response.header." + name, response.header(name));
                }
            }

            if (captureResponseBody && null != response.body()) {
                String body = responseBody2String(response.body());
                if (null != body) {
                    attributes.put("http.response.body", body);
                }
            }
        }

        private String requestBody2String(RequestBody body) {
            try {
                Buffer buffer = new Buffer();
                body.writeTo(buffer);
                return buffer.readUtf8();
            } catch (IOException e) {
                return null;
            }
        }

        private String responseBody2String(ResponseBody responseBody) {
            try {
                BufferedSource source = responseBody.source();
                source.request(Integer.MAX_VALUE);
                Buffer buffer = source.buffer();
                //noinspection CharsetObjectCanBeUsed
                Charset charset = Charset.forName("UTF-8");
                MediaType contentType = responseBody.contentType();
                if (null != contentType) {
                    charset = contentType.charset(charset);
                }
                String body = buffer.clone().readString(charset);

                // limit response body to 50 KB
                final int maxLength = Math.min(50 * 1024, body.length());
                return body.substring(0, maxLength);
            } catch (IOException e) {
                // ignore
                return null;
            }
        }
    }
}
