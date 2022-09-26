package com.aliyun.sls.android.trace.instrument;

import java.util.Map;

/**
 * @author gordon
 * @date 2022/9/15
 */
public interface HttpInstrumentationDelegate<T> extends InstrumentationDelegate<T> {
    Map<String, String> injectCustomHeaders(T t);
}
