package com.aliyun.sls.android.okhttp.instrumentation;

import java.util.List;

import io.opentelemetry.instrumentation.api.instrumenter.http.HttpClientAttributesGetter;
import io.opentelemetry.instrumentation.api.instrumenter.http.HttpServerAttributesGetter;
import okhttp3.Request;
import okhttp3.Response;

/**
 * @author yulong.gyl
 * @date 2023/9/25
 */
public enum OkHttpAttributesGetter implements HttpClientAttributesGetter<Request, Response>,
    HttpServerAttributesGetter<Request, Response> {
    INSTANCE;

    @Override
    public String getHttpRequestMethod(Request request) {
        return request.method();
    }

    @Override
    public String getUrlFull(Request request) {
        return request.url().toString();
    }

    @Override
    public List<String> getHttpRequestHeader(Request request, String name) {
        return request.headers(name);
    }

    @Override
    public Integer getHttpResponseStatusCode(
        Request request, Response response, Throwable error) {
        return response.code();
    }

    @Override
    public List<String> getHttpResponseHeader(Request request, Response response, String name) {
        return response.headers(name);
    }

    @Override
    public String getNetworkProtocolName(Request request, Response response) {
        if (response == null) {
            return null;
        }
        switch (response.protocol()) {
            case HTTP_1_0:
            case HTTP_1_1:
            case HTTP_2:
                return "http";
            case SPDY_3:
                return "spdy";
        }
        return null;
    }

    @Override
    public String getNetworkProtocolVersion(Request request, Response response) {
        if (response == null) {
            return null;
        }
        switch (response.protocol()) {
            case HTTP_1_0:
                return "1.0";
            case HTTP_1_1:
                return "1.1";
            case HTTP_2:
                return "2";
            case SPDY_3:
                return "3.1";
        }
        return null;
    }

    @Override

    public String getServerAddress(Request request) {
        return request.url().host();
    }

    @Override
    public Integer getServerPort(Request request) {
        return request.url().port();
    }

    @Override
    public String getUrlScheme(Request request) {
        return request.url().scheme();
    }

    @Override
    public String getUrlPath(Request request) {
        return request.url().encodedPath();
    }

    @Override
    public String getUrlQuery(Request request) {
        return request.url().query();
    }

    @Override
    public String getHttpRoute(Request request) {
        return request.url().encodedPath();
    }

    @Override
    public String getTransport(Request request, Response response) {
        return HttpClientAttributesGetter.super.getTransport(request, response);
    }

    @Override
    public String getSockFamily(Request request, Response response) {
        return HttpClientAttributesGetter.super.getSockFamily(request, response);
    }

    @Override
    public String getNetworkType(Request request, Response response) {
        return HttpClientAttributesGetter.super.getNetworkType(request, response);
    }
}
