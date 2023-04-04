package com.aliyun.sls.android.trace.instrument;

import java.util.Map;

import com.aliyun.sls.android.ot.Span;

/**
 * @author gordon
 * @date 2022/9/15
 */
public interface HttpInstrumentationDelegate<T> extends InstrumentationDelegate<T> {
    /**
     * Inject customization span to the request header.
     * @param t http request
     * @return header map will be injected
     */
    Map<String, String> injectCustomHeaders(T t);

    /**
     * Customize the http request span name
     * @param request http request
     * @return the span name
     */
    String nameSpan(T request);

    /**
     * Customize the http request span will be reported
     * @param request http request
     * @param span http request span
     */
    void customizeSpan(T request, Span span);
}
