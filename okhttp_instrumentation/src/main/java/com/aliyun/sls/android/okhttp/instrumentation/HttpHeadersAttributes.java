package com.aliyun.sls.android.okhttp.instrumentation;

import java.nio.charset.Charset;

import io.opentelemetry.api.common.AttributesBuilder;
import io.opentelemetry.context.Context;
import io.opentelemetry.instrumentation.api.instrumenter.AttributesExtractor;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.Buffer;
import okio.BufferedSource;

/**
 * @author yulong.gyl
 * @date 2023/10/17
 */
class HttpHeadersAttributes implements AttributesExtractor<Request, Response> {
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
        if (null == request) {
            return;
        }

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
        if (null == response) {
            return;
        }

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
        } catch (Throwable e) {
            e.printStackTrace();
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
        } catch (Throwable e) {
            // ignore
            e.printStackTrace();
            return null;
        }
    }
}